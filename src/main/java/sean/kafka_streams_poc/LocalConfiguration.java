package sean.kafka_streams_poc;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;

@Configuration
@Profile("local")
public class LocalConfiguration {

	@Bean
	public ApiInfo apiInfo() {
		return new ApiInfo("Kafka Streams POC API - LOCAL",
				           "To query Kafka Streams store",
				           "0.1",
				           "Demo only",
				           new Contact("Sean", "http://www.sean.com", "sean@sean.com"),
				           "Null License",
				           "http://www.sean.com/license",
				           Collections.emptyList());
	}
	
}
