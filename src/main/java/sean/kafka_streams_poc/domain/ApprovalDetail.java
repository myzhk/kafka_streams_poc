package sean.kafka_streams_poc.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import sean.kafka_streams_poc.serdes.JSONSerdeCompatible;

public class ApprovalDetail implements JSONSerdeCompatible {
	public String allocId; // whatever ID we sent to Taser to identify an allocation
	public Entity entity;
	public String taserApprovalId;
	public String economics;
	
	public ApprovalDetail() {}
	
    public ApprovalDetail(String allocId, Entity entity, String taserApprovalId, String economics) {
		super();
		this.allocId = allocId;
		this.entity = entity;
		this.taserApprovalId = taserApprovalId;
		this.economics = economics;
	}

	@Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
