package sean.kafka_streams_poc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import sean.kafka_streams_poc.streams.ApprovalCacheProcessor;

@SpringBootTest
class KafkaStreamsPocApplicationTests {

	// have a mock will disable starting the Streams application for TEST
	// since no real test is implemented so far, it's annoying to start Streams during the "build" gradle task
	@MockBean
	private ApprovalCacheProcessor streamsProcessor;
	
	@Test
	void contextLoads() {
	}

}
