package sean.kafka_streams_poc.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.ImmutableList;

import sean.kafka_streams_poc.serdes.JSONSerdeCompatible;

public class ApprovalDetail implements JSONSerdeCompatible {
	public String allocId; // whatever ID we sent to Taser to identify an allocation
	public Entity entity;
	public String taserApprovalId;
	public String economics;
	public boolean marked;
	
	public ApprovalDetail() {}
	
    public ApprovalDetail(String allocId, Entity entity, String taserApprovalId, String economics) {
		super();
		this.allocId = allocId;
		this.entity = entity;
		this.taserApprovalId = taserApprovalId;
		this.economics = economics;
	}
    
    public ApprovalDetails asApprovalDetails() {
    	return new ApprovalDetails(this.allocId, TokenType.AllocToken, this.entity, ImmutableList.of(this));
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
