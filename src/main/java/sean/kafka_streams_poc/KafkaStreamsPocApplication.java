package sean.kafka_streams_poc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class KafkaStreamsPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsPocApplication.class, args);
	}

	@Autowired
	private ApiInfo apiInfo;
	
	@Bean
	public Docket swaggerConfig() {
		return new Docket(DocumentationType.OAS_30).select()
                                                   .paths(PathSelectors.ant("/approval-details/**"))
                                                   .apis(RequestHandlerSelectors.basePackage("sean.kafka_streams_poc"))
                                                   .build()
                                                   .apiInfo(apiInfo);
	}
	
}
