package sean.kafka_streams_poc.domain;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ApprovalDetailsWithProcessingInstruction extends ApprovalDetails {
	public boolean delete;
	
	public ApprovalDetailsWithProcessingInstruction() {}
	
	public ApprovalDetailsWithProcessingInstruction(boolean delete, String tokenId, TokenType type, Entity entity, List<ApprovalDetail> approvalDetails) {
		super(tokenId, type, entity, approvalDetails);
		this.delete = delete;
	}
	
    public ApprovalDetailsWithProcessingInstruction(boolean delete, Token token, List<ApprovalDetail> approvalDetails) {
    	super(token, approvalDetails);
    	this.delete = delete;
	}

    public ApprovalDetails getApprovalDetails() {
    	return new ApprovalDetails(this.token, this.approvalDetails);
    }
    
	@Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
