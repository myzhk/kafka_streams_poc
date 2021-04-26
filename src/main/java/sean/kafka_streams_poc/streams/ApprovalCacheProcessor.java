package sean.kafka_streams_poc.streams;

import static com.google.common.base.Verify.verify;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
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
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import sean.kafka_streams_poc.domain.ApprovalCancel;
import sean.kafka_streams_poc.domain.ApprovalDetail;
import sean.kafka_streams_poc.domain.ApprovalDetails;
import sean.kafka_streams_poc.domain.ApprovalDetailsWithProcessingInstruction;
import sean.kafka_streams_poc.domain.Token;
import sean.kafka_streams_poc.domain.TokenType;
import sean.kafka_streams_poc.serdes.JSONSerde;

@Component
public class ApprovalCacheProcessor implements SmartLifecycle {

	static final Logger LOG = LoggerFactory.getLogger(ApprovalCacheProcessor.class);
	
	static final CancelJoiner CANCEL_LEFT_JOINER = new CancelJoiner();
	static final ToCacheEntryMapper TO_CACHE_ENTRY_MAPPER = new ToCacheEntryMapper();
	
	private volatile boolean running;
	private KafkaStreams streams;
	private ReadOnlyKeyValueStore<Token, ApprovalDetails> store;
	
	private KafkaProducer<Token, ApprovalDetails> producer;
	
	@PostConstruct
    private void init() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "approval-cache-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/home/Pi/tmp/kafka-streams");
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_BETA);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, JSONSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JSONSerde.class);
        
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<Token, ApprovalDetails> approvedStream = builder.stream("bbg-approved");
        KStream<Token, ApprovalCancel> cancelStream = builder.stream("bbg-cancel");

        approvedStream.leftJoin(cancelStream, 
        		                CANCEL_LEFT_JOINER, 
        		                JoinWindows.of(Duration.ofMinutes(2)))
                      .flatMap(TO_CACHE_ENTRY_MAPPER)
                      .to("cache-operations");
      
        // KTable is always timestamped!
        // https://kafka.apache.org/documentation/streams/developer-guide/processor-api.html#timestamped-state-stores
        // see {@link TableSourceNode#writeToTopology(InternalTopologyBuilder)}
        //
        // also, global table always has "auto.offset.reset" set to "earliest"
        builder.globalTable("cache-operations", Materialized.<Token, ApprovalDetails, KeyValueStore<Bytes, byte[]>>as("approval-cache"));
        
        final Topology topology = builder.build();
        streams = new KafkaStreams(topology, props);
        
        LOG.info(topology.describe().toString());
        
		Properties producerProps = new Properties();
		producerProps.put("bootstrap.servers", "localhost:9092");
		producerProps.put("enable.idempotence", true);
		producerProps.put("key.serializer", JSONSerde.class);
		producerProps.put("value.serializer", JSONSerde.class);

		this.producer = new KafkaProducer<>(producerProps);
    }
	
	@Override
	public void stop() {
		this.running = false;
		streams.close();
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
    	streams.start();
        
        // see {@link GlobalStateStoreProvider#stores(String, QueryableStoreType)} for how StateStoreProvider wraps an read-only facade on top of the underlying store
        store = streams.store(StoreQueryParameters.fromNameAndType("approval-cache", QueryableStoreTypes.<Token, ApprovalDetails>keyValueStore()));
        
        // this is a write-able store, but do we really want to do it?
        // MeteredTimestampedKeyValueStore<Token, ApprovalDetails> store = streams.store(StoreQueryParameters.fromNameAndType("approval-cache", new MeteredTimestampedKeyValueStoreType<>()));
        
        this.running = true;
    }
    
	// store is guaranteed to be available when REST controller accesses it, because web server's start phase is Integer.MAX_VALUE - 1
	// see {@link WebServerStartStopLifecycle}
    public ReadOnlyKeyValueStore<Token, ApprovalDetails> getReadOnlyStore() {
		return store;
	}
    
    public void updateApprovalDetails(ApprovalDetails ad) {
    	producer.send(new ProducerRecord<Token, ApprovalDetails>("cache-operations", ad.token, ad));
    }
    
    static class CancelJoiner implements ValueJoiner<ApprovalDetails, ApprovalCancel, ApprovalDetailsWithProcessingInstruction> {
		@Override
		public ApprovalDetailsWithProcessingInstruction apply(ApprovalDetails value1, ApprovalCancel value2) {
			verify(value1.token != null && value1.token.type == TokenType.EventToken, "[%s] input approval token's type is not %s", this.getClass().getSimpleName(), TokenType.EventToken);
			return new ApprovalDetailsWithProcessingInstruction(value2 == null ? false : true, value1.token, value1.approvalDetails);
		}
    }
    
    static class ToCacheEntryMapper implements KeyValueMapper<Token, ApprovalDetailsWithProcessingInstruction, List<KeyValue<Token, ApprovalDetails>>> {
		@Override
		public List<KeyValue<Token, ApprovalDetails>> apply(Token token, ApprovalDetailsWithProcessingInstruction detailsWithInstruction) {
			verify(token.type == TokenType.EventToken, "[%s] key Token's type is not %s", this.getClass().getSimpleName(), TokenType.EventToken);
			verify(Objects.equal(token, detailsWithInstruction.token), "[%s] Token in ApprovalDetails does not match Token key", this.getClass().getSimpleName());
			verify(CollectionUtils.isNotEmpty(detailsWithInstruction.approvalDetails), "[%s] input ApprovalDetails is empty", this.getClass().getSimpleName());
			
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