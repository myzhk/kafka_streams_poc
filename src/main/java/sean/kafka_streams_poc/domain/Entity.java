package sean.kafka_streams_poc.domain;

public enum Entity {
	Bloomberg,
	TradeWeb,
	Traiana;
	
	public static Entity parse(String val) {
		for (Entity en: Entity.values()) {
			if (en.name().equalsIgnoreCase(val)) {
				return en;
			}
		}
		return null;
	}
}
