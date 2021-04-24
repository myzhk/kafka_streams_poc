package sean.kafka_streams_poc.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import sean.kafka_streams_poc.serdes.JSONSerdeCompatible;

public class Token implements JSONSerdeCompatible {
    public String tokenId;
    public TokenType type;
    public Entity entity;
    
    public Token() {}
    
    public Token(String tokenId, TokenType type, Entity entity) {
		super();
		this.tokenId = tokenId;
		this.type = type;
		this.entity = entity;
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
