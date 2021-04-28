package sean.kafka_streams_poc.streams;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.internals.MeteredTimestampedKeyValueStore;
import org.apache.kafka.streams.state.internals.StateStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import sean.kafka_streams_poc.domain.ApprovalCancel;
import sean.kafka_streams_poc.domain.ApprovalDetail;
import sean.kafka_streams_poc.domain.ApprovalDetails;
import sean.kafka_streams_poc.domain.ApprovalDetailsWithProcessingInstruction;
import sean.kafka_streams_poc.domain.Token;
import sean.kafka_streams_poc.domain.TokenType;

@Component
public class ApprovalCacheProcessor implements SmartLifecycle {

	static final Logger LOG = LoggerFactory.getLogger(ApprovalCacheProcessor.class);
	static final CancelJoiner CANCEL_LEFT_JOINER = new CancelJoiner();
	static final ToCacheEntryMapper TO_CACHE_ENTRY_MAPPER = new ToCacheEntryMapper();
	
	private volatile boolean running;
	
	private KafkaStreams streams;
	
	private String cacheTopic;
	private KafkaProducer<Token, ApprovalDetails> producer;
	
	private String storeName;
	private ReadOnlyKeyValueStore<Token, ApprovalDetails> store;
	
	@Inject
	public ApprovalCacheProcessor(@Value("#{${streamsProps}}") Map<String, String> streamsProps,
                                  @Value("#{${producerProps}}") Map<String, String> producerProps,
								  @Value("${topic.approved}") String approvedTopic,
								  @Value("${topic.invalid}") String invalidTopic,
								  @Value("${topic.cancel}") String cancelTopic,
								  @Value("${topic.cache}") String cacheTopic,
								  @Value("${store.name}") String storeName) {
		
		this.cacheTopic = cacheTopic; 
		
        final StreamsBuilder builder = new StreamsBuilder();
        
        KStream<Token, ApprovalCancel> cancelStream = builder.stream(cancelTopic);
        
        // 2.7 code
        /*
        @SuppressWarnings("unchecked")
		KStream<Token, ApprovalDetails>[] branches = builder.<Token, ApprovalDetails>stream(approvedTopic)
        		.branch((t, ad) -> t == null ||
        		                   ad == null ||
        		                   TokenType.EventToken != t.type ||
        		                   !Objects.equals(t, ad.token) ||
        		                   CollectionUtils.isEmpty(ad.approvalDetails),
        		        (t, ad) -> true);
        
        branches[0].to(invalidTopic);
        branches[1].leftJoin(cancelStream,
                             CANCEL_LEFT_JOINER,
                             JoinWindows.of(Duration.ofMinutes(2)))
                   .flatMap(TO_CACHE_ENTRY_MAPPER)
                   .to(cacheTopic);
        */
        
        Map<String, KStream<Token, ApprovalDetails>> approvedStreamMap = builder.<Token, ApprovalDetails>stream(approvedTopic)
        		.split(Named.as(approvedTopic + "-"))
        		.branch((t, ad) -> t == null ||
                                   ad == null ||
                                   TokenType.EventToken != t.type ||
                                   !Objects.equals(t, ad.token) ||
                                   CollectionUtils.isEmpty(ad.approvalDetails), 
                        Branched.as("invalid"))
        		.defaultBranch(Branched.as("valid"));
        
        approvedStreamMap.get(approvedTopic + "-invalid").to(invalidTopic);
        approvedStreamMap.get(approvedTopic + "-valid")
                         .leftJoin(cancelStream,
                                   CANCEL_LEFT_JOINER,
                                   JoinWindows.of(Duration.ofMinutes(2)))
                         .flatMap(TO_CACHE_ENTRY_MAPPER)
                         .to(cacheTopic);
        
        // global table always has "auto.offset.reset" set to "earliest"
        // hence if you delete the state dir, it will re-build from earliest messages
        this.storeName = storeName;
        builder.globalTable(cacheTopic, Materialized.<Token, ApprovalDetails, KeyValueStore<Bytes, byte[]>>as(storeName));
        
        final Topology topology = builder.build();
        this.streams = new KafkaStreams(topology, MapUtils.toProperties(streamsProps));
        LOG.info(topology.describe().toString());
        
		this.producer = new KafkaProducer<>(MapUtils.toProperties(producerProps));
    }
	
	@Override
	public void stop() {
		this.running = false;
		this.producer.close();
		this.streams.close();
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}
    
	// right before WebServer starts up
	// see {@link WebServerStartStopLifecycle}
	@Override
	public int getPhase() {
		return Integer.MAX_VALUE - 2;
	}
	
	@Override
	public void start() {
    	this.streams.start();

        this.store = streams.store(StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.<Token, ApprovalDetails>keyValueStore()));

        this.running = true;

        /* see {@link GlobalStateStoreProvider#stores(String, QueryableStoreType)} for how StateStoreProvider wraps an read-only facade on top of the underlying store
           by supplying a custom QueryableStoreType, we can get a write-able store, but do we really want to do it??
           
           also, table is always timestamped!
           see https://kafka.apache.org/documentation/streams/developer-guide/processor-api.html#timestamped-state-stores, and
           see {@link TableSourceNode#writeToTopology(InternalTopologyBuilder)} */
        
        // MeteredTimestampedKeyValueStore<Token, ApprovalDetails> store = streams.store(StoreQueryParameters.fromNameAndType("approval-cache", new MeteredTimestampedKeyValueStoreType<>()));
        // store.put(new Token("789", TokenType.AllocToken, Entity.Bloomberg), ValueAndTimestamp.make(new ApprovalDetails("789_sean_hack", TokenType.AllocToken, Entity.Bloomberg, null), Time.SYSTEM.milliseconds()));
    }
    
	// store is guaranteed to be available when REST controller accesses it, because web server's start phase is Integer.MAX_VALUE - 1
	// see {@link WebServerStartStopLifecycle}
    public ReadOnlyKeyValueStore<Token, ApprovalDetails> getReadOnlyStore() {
		return store;
	}
    
    public void updateApprovalDetails(ApprovalDetails ad) {
    	producer.send(new ProducerRecord<Token, ApprovalDetails>(this.cacheTopic, ad.token, ad));
    }
    
    static class CancelJoiner implements ValueJoiner<ApprovalDetails, ApprovalCancel, ApprovalDetailsWithProcessingInstruction> {
		@Override
		public ApprovalDetailsWithProcessingInstruction apply(ApprovalDetails value1, ApprovalCancel value2) {
			// to reproduce a RecordTooLargeException, and the app to fail to restart due to offset/highWatermark inconsistency - see {@link GlobalStateManagerImpl#restoreState}
			/*ApprovalDetailsWithProcessingInstruction adsi = new ApprovalDetailsWithProcessingInstruction(false, value1.token, new ArrayList<>());
			for (int i = 0; i < 10000; i++) {
				adsi.approvalDetails.add(value1.approvalDetails.get(0));
			}
			return adsi; */
			
			return new ApprovalDetailsWithProcessingInstruction(value2 == null ? false : true, value1.token, value1.approvalDetails);
		}
    }
    
    static class ToCacheEntryMapper implements KeyValueMapper<Token, ApprovalDetailsWithProcessingInstruction, List<KeyValue<Token, ApprovalDetails>>> {
		@Override
		public List<KeyValue<Token, ApprovalDetails>> apply(Token token, ApprovalDetailsWithProcessingInstruction detailsWithInstruction) {
			List<KeyValue<Token, ApprovalDetails>> result = new ArrayList<>(detailsWithInstruction.approvalDetails.size());
			result.add(KeyValue.pair(token, detailsWithInstruction.delete ? null : detailsWithInstruction.getApprovalDetails()));
			
			for (ApprovalDetail allocAd: detailsWithInstruction.approvalDetails) {
				Token allocToken = new Token(allocAd.allocId, TokenType.AllocToken, allocAd.entity);
				ApprovalDetails allocAds = detailsWithInstruction.delete ? null : new ApprovalDetails(allocToken, ImmutableList.of(allocAd));
				result.add(KeyValue.pair(allocToken, allocAds));
			}
			
			return result;
		}
    }
    
    static class MeteredTimestampedKeyValueStoreType<K, V> implements QueryableStoreType<MeteredTimestampedKeyValueStore<Token, ApprovalDetails>> {

		@Override
		public boolean accepts(StateStore stateStore) {
			return MeteredTimestampedKeyValueStore.class.isAssignableFrom(stateStore.getClass());
		}

		@Override
		public MeteredTimestampedKeyValueStore<Token, ApprovalDetails> create(StateStoreProvider storeProvider, String storeName) {
			return storeProvider.stores(storeName, this).get(0);
		}
    }

}