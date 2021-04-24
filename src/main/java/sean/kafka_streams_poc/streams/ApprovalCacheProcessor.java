package sean.kafka_streams_poc.streams;

import static com.google.common.base.Verify.verify;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections4.CollectionUtils;
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
import org.springframework.boot.CommandLineRunner;
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
public class ApprovalCacheProcessor implements CommandLineRunner {

	static final Logger LOG = LoggerFactory.getLogger(ApprovalCacheProcessor.class);
	
	static final CancelJoiner CANCEL_LEFT_JOINER = new CancelJoiner();
	static final ToCacheEntryMapper TO_CACHE_ENTRY_MAPPER = new ToCacheEntryMapper();
	
	private volatile boolean started;
	private KafkaStreams streams;
	private ReadOnlyKeyValueStore<Token, ApprovalDetails> store;
	
	@PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "approval-cache-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
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
        started = false;
        
        LOG.info(topology.describe().toString());
    }
	
    public void run(String... args) {
    	streams.start();
    	started = true;
        
        // see {@link GlobalStateStoreProvider#stores(String, QueryableStoreType)} for how StateStoreProvider wraps an read-only facade on top of the underlying store
        store = streams.store(StoreQueryParameters.fromNameAndType("approval-cache", QueryableStoreTypes.<Token, ApprovalDetails>keyValueStore()));
        
        // this is a write-able store, but do we really want to do it?
        // MeteredTimestampedKeyValueStore<Token, ApprovalDetails> store = streams.store(StoreQueryParameters.fromNameAndType("approval-cache", new MeteredTimestampedKeyValueStoreType<>()));        
    }
    
    public ReadOnlyKeyValueStore<Token, ApprovalDetails> getStore() {
    	verify(started, "streams has not started yet");
		return store;
	}

	@PreDestroy
    public void destroy() {
    	streams.close();
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
		public List<KeyValue<Token, ApprovalDetails>> apply(Token key, ApprovalDetailsWithProcessingInstruction value) {
			verify(key.type == TokenType.EventToken, "[%s] key Token's type is not %s", this.getClass().getSimpleName(), TokenType.EventToken);
			verify(Objects.equal(key, value.token), "[%s] Token in ApprovalDetails does not match Token key", this.getClass().getSimpleName());
			verify(CollectionUtils.isNotEmpty(value.approvalDetails), "[%s] input ApprovalDetails is empty", this.getClass().getSimpleName());
			
			List<KeyValue<Token, ApprovalDetails>> result = new ArrayList<>(value.approvalDetails.size());
			result.add(KeyValue.pair(key, value.delete ? null : value.getApprovalDetails()));
			
			for (ApprovalDetail ad: value.approvalDetails) {
				Token allocToken = new Token(ad.allocId, TokenType.AllocToken, key.entity);
				ApprovalDetails allocApprovalDetails = value.delete ? null : new ApprovalDetails(allocToken, ImmutableList.of(ad));
				result.add(KeyValue.pair(allocToken, allocApprovalDetails));
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