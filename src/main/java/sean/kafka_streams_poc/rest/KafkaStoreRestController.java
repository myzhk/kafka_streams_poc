package sean.kafka_streams_poc.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.apache.kafka.streams.state.KeyValueIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.swagger.annotations.ApiOperation;
import sean.kafka_streams_poc.domain.ApprovalDetail;
import sean.kafka_streams_poc.domain.ApprovalDetails;
import sean.kafka_streams_poc.domain.Entity;
import sean.kafka_streams_poc.domain.Token;
import sean.kafka_streams_poc.domain.TokenType;
import sean.kafka_streams_poc.streams.ApprovalCacheProcessor;

@RestController
@RequestMapping("/approval-details")
public class KafkaStoreRestController {
	
	static final Logger LOG = LoggerFactory.getLogger(KafkaStoreRestController.class);
	
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
		return approvalStreamsProcessor.getReadOnlyStore().get(token);
	}
	
	@PostMapping(value="/{economicsDetail}",
			     produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Find alloc-level ApprovalDetails by event-level Token and matching economics",
	              notes = "This call has side effect! If a match is found, it will be marked as used! " +
	            		  "Should only be used by the Event token type. " +
	              		  "Need to supply both Token (in POST payload) and economics (on the URL path).",
	              response = ApprovalDetails.class)
	public ApprovalDetails findApprovalDetailsWithEventTokenAndEconomics(@RequestBody Token token,
                                                                         @PathVariable String economicsDetail) throws Exception {
		if (TokenType.AllocToken == token.type) {
			return findApprovalDetails(token.entity.name(), token.type.name(), token.tokenId);
		}
		
		AtomicBoolean inUse = tokenLocks.get(token);
		
		while (!inUse.compareAndSet(false, true));
		
		try {
			ApprovalDetails ads = approvalStreamsProcessor.getReadOnlyStore().get(token);
			if (ads == null) return null;
			
			ApprovalDetail allocAd = null;
			for (ApprovalDetail ad: ads.approvalDetails) {
				if (Objects.equals(ad.economics, economicsDetail) && !ad.marked) {
					allocAd = ad;
					break;
				}
			}

			if (allocAd == null) return null;
			
			allocAd.marked = true;
			approvalStreamsProcessor.updateApprovalDetails(ads);
			LOG.info("produced cache update message for {}", ads);

			ApprovalDetails newAds = null;
			do {
				TimeUnit.MILLISECONDS.sleep(10); // is this appropriate?
				newAds = approvalStreamsProcessor.getReadOnlyStore().get(token);
			} while (!ads.equals(newAds));
			LOG.info("found updated entry in cache for {}", ads);

			return allocAd.asApprovalDetails();
		} finally {
			inUse.set(false);
		}
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
