package info.flexmojos.generator;

import java.util.Map;

public class SimplePojo {

	private String name;

	private Double value;

	private Map nicknames;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Map getNicknames() {
		return nicknames;
	}

	public void setNicknames(Map nicknames) {
		this.nicknames = nicknames;
	}

}
