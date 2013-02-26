package org.langke.jetty.common;

import java.io.*;

public class StringPair implements Serializable {
	private static final long serialVersionUID = 3454234325353654757L;
	private String name;
	private String value;

	public StringPair() {
	}

	public StringPair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
