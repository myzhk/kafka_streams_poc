package sean.kafka_streams_poc.domain;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import sean.kafka_streams_poc.serdes.JSONSerdeCompatible;

// could either store event-level (multiple) or alloc-level (single) approval detail(s)
public class ApprovalDetails implements JSONSerdeCompatible {
	public Token token;
	public List<ApprovalDetail> approvalDetails;
	
	public ApprovalDetails() {}
	
	public ApprovalDetails(String tokenId, TokenType type, Entity entity, List<ApprovalDetail> approvalDetails) {
		this.token = new Token(tokenId, type, entity);
		this.approvalDetails = approvalDetails;
	}
	
    public ApprovalDetails(Token token, List<ApprovalDetail> approvalDetails) {
		this.token = token;
		this.approvalDetails = approvalDetails;
	}
    
    @Override
    public int hashCode() {
    	return HashCodeBuilder.reflectionHashCode(this);
    }

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
	
	@Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
