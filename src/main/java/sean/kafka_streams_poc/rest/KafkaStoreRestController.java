package sean.kafka_streams_poc.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.kafka.streams.state.KeyValueIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.swagger.annotations.ApiOperation;
import sean.kafka_streams_poc.domain.ApprovalDetails;
import sean.kafka_streams_poc.domain.Entity;
import sean.kafka_streams_poc.domain.Token;
import sean.kafka_streams_poc.domain.TokenType;
import sean.kafka_streams_poc.streams.ApprovalCacheProcessor;

@RestController
@RequestMapping("/approval-details")
public class KafkaStoreRestController {
	
	private LoadingCache<Token, AtomicBoolean> tokenLocks;
	
	@Autowired
	private ApprovalCacheProcessor approvalStreamsProcessor;
	
	@PostConstruct
	private void init() {
		this.tokenLocks = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.HOURS)
				.build(new CacheLoader<Token, AtomicBoolean>() {
					@Override
					public AtomicBoolean load(Token token) throws Exception {
						return new AtomicBoolean(false);
					}
				});
	}
	
	@GetMapping(value="/{entity}/{tokenType}/{tokenId}",
			    produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Find ApprovalDetails by Token",
	              notes = "Need to supply all Token info - Entity, Type, and ID",
	              response = ApprovalDetails.class)
	public ApprovalDetails findApprovalDetails(@PathVariable String entity,
												@PathVariable String tokenType, 
												@PathVariable String tokenId) throws Exception {
		Token token = new Token(tokenId, TokenType.parse(tokenType), Entity.parse(entity));
		AtomicBoolean inUse = tokenLocks.get(token);
		while (!inUse.compareAndSet(false, true));
		
		TimeUnit.SECONDS.sleep(10);
		ApprovalDetails ad = approvalStreamsProcessor.getReadOnlyStore().get(token); 
		
		inUse.set(false);
		return ad;
	}
	
	@GetMapping(value="/",
			    produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Find all ApprovalDetails",
	              notes = "No input parameter needed",
	              response = List.class)
	public List<ApprovalDetails> findAllApprovalDetails() {
		List<ApprovalDetails> result = new ArrayList<>();
		KeyValueIterator<Token, ApprovalDetails> itr = approvalStreamsProcessor.getReadOnlyStore().all(); 
		while (itr.hasNext()) {
			result.add(itr.next().value);
		}
		return result;
	}
	
}
