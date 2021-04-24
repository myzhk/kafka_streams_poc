package sean.kafka_streams_poc.serdes;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import sean.kafka_streams_poc.domain.ApprovalCancel;
import sean.kafka_streams_poc.domain.ApprovalDetail;
import sean.kafka_streams_poc.domain.ApprovalDetails;
import sean.kafka_streams_poc.domain.ApprovalDetailsWithProcessingInstruction;
import sean.kafka_streams_poc.domain.Token;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_t")
@JsonSubTypes({
                  @JsonSubTypes.Type(value = Token.class, name = "tk"),
                  @JsonSubTypes.Type(value = ApprovalDetail.class, name = "ad"),
                  @JsonSubTypes.Type(value = ApprovalDetails.class, name = "as"),
                  @JsonSubTypes.Type(value = ApprovalCancel.class, name = "ac"),
                  @JsonSubTypes.Type(value = ApprovalDetailsWithProcessingInstruction.class, name = "ai")
              })
public interface JSONSerdeCompatible {

}
