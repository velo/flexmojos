package info.flexmojos.generator;

import java.util.List;

public class SimplePojo {

	private String name;
	
	private Double value;
	
	private List nicknames;

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

	public List getNicknames() {
		return nicknames;
	}

	public void setNicknames(List nicknames) {
		this.nicknames = nicknames;
	}
	
}
