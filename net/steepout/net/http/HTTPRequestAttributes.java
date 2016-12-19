package net.steepout.net.http;

import java.util.HashMap;

import net.steepout.net.Attributes;

public class HTTPRequestAttributes extends HashMap<String, String> implements Attributes {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4678529119792838631L;

	public static HTTPRequestAttributes parse(String string) {
		HTTPRequestAttributes attr = new HTTPRequestAttributes();
		attr.load(string);
		return attr;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (Entry<String, String> entry : this.entrySet()) {
			buffer.append(entry.getKey());
			buffer.append('=');
			buffer.append(entry.getValue());
			buffer.append("&");
		}
		return buffer.substring(0, buffer.length() - 2);
	}

	@Override
	public void load(String data) {
		int split = 0;
		for (String str : data.split("&")) {
			if(str.trim().isEmpty())
				continue;
			split = str.indexOf('=');
			put(str.substring(0, split).trim(), str.substring(split + 1).trim());
		}
	}

}
