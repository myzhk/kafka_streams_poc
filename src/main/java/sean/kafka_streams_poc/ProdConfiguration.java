package sean.kafka_streams_poc;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;

@Configuration
@Profile("prod")
public class ProdConfiguration {

	@Bean
	public ApiInfo apiInfo() {
		return new ApiInfo("Kafka Streams POC API - PROD",
				           "To query Kafka Streams store",
				           "0.1",
				           "Demo only",
				           new Contact("Sean", "http://www.sean.com", "sean@sean.com"),
				           "Null License",
				           "http://www.sean.com/license",
				           Collections.emptyList());
	}
}