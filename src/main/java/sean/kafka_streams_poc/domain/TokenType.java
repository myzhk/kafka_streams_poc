package sean.kafka_streams_poc.domain;

public enum TokenType {
	EventToken,
	AllocToken;
	
	public static TokenType parse(String val) {
		for (TokenType tt: TokenType.values()) {
			if (tt.name().equalsIgnoreCase(val)) {
				return tt;
			}
		}
		return null;
	}
}
