package sean.kafka_streams_poc.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import sean.kafka_streams_poc.domain.ApprovalDetails;
import sean.kafka_streams_poc.domain.Entity;
import sean.kafka_streams_poc.domain.Token;
import sean.kafka_streams_poc.domain.TokenType;
import sean.kafka_streams_poc.streams.ApprovalCacheProcessor;

@RestController
public class KafkaStoreRestController {
	
	@Autowired
	private ApprovalCacheProcessor approvalStreamsProcessor;
	
	@GetMapping(value="/approval-details/{entity}/{tokenType}/{tokenId}")
	public ApprovalDetails findApprovalDetails(@PathVariable String entity,
												@PathVariable String tokenType, 
												@PathVariable String tokenId) {
		return approvalStreamsProcessor.getStore().get(new Token(tokenId, TokenType.parse(tokenType), Entity.parse(entity)));
	}
	
}
