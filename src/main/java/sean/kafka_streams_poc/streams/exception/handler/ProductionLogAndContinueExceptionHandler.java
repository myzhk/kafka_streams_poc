package sean.kafka_streams_poc.streams.exception.handler;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.errors.ProductionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sean.kafka_streams_poc.serdes.JSONSerde;

public class ProductionLogAndContinueExceptionHandler implements ProductionExceptionHandler {

	static final Logger LOG = LoggerFactory.getLogger(ProductionLogAndContinueExceptionHandler.class);
	
	@Override
	public void configure(Map<String, ?> configs) {	}

	@Override
	public ProductionExceptionHandlerResponse handle(ProducerRecord<byte[], byte[]> record, Exception exception) {
		LOG.warn("Exception caught during processing, " +
                "topic: {}, partition: {}, key: {}, ",
                record.topic(), record.partition(), 
                JSONSerde.getInstance().deserialize(record.topic(), record.key()).toString(),
                exception);
		return ProductionExceptionHandlerResponse.CONTINUE;
	}
	
}
