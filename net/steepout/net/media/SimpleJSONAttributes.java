package net.steepout.net.media;

import java.util.HashMap;

import net.steepout.net.Attributes;

public class SimpleJSONAttributes extends HashMap<String, String> implements Attributes {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5784361473170970010L;

	public static SimpleJSONAttributes parse(String string) {
		SimpleJSONAttributes attr = new SimpleJSONAttributes();
		attr.load(string);
		return attr;
	}

	public String toString() {
		if (isEmpty())
			return "";
		StringBuffer buffer = new StringBuffer();
		for (Entry<String, String> entry : this.entrySet()) {
			buffer.append('"');
			buffer.append(entry.getKey());
			buffer.append('"');
			buffer.append(": ");
			buffer.append('"');
			buffer.append(entry.getValue());
			buffer.append('"');
			buffer.append(',');
		}
		return '{' + buffer.substring(0, buffer.length() - 1) + '}';
	}

	private String trim(String str) {
		if (str.length() <= 2)
			return str.trim();
		return str.substring(1, str.length() - 1).trim();
	}

	@Override
	public void load(String data) {
		int split = 0;
		data = data.substring(1, data.length() - 1).trim();
		for (String str : data.split(",")) {
			if (str.trim().isEmpty())
				continue;
			split = str.indexOf(':');
			put(trim(str.substring(0, split)), trim(str.substring(split + 1).trim()));
		}
	}

}
