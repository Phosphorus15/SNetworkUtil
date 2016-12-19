package net.steepout.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;

import net.steepout.net.http.HTTPRequest.Method;

public class HTTPResponse extends HashMap<String, Object> implements HTTPMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5977859614654740758L;

	String version;

	int statusCode;

	String statusDescription;

	byte[] body;

	public HTTPResponse() {
		this("OK");
	}

	public HTTPResponse(String statusDescription) {
		this("1.0", statusDescription);
	}

	public HTTPResponse(String version, String statusDescription) {
		this(200, version, statusDescription);
	}

	public HTTPResponse(int statusCode, String version, String statusDescription) {
		this.statusCode = statusCode;
		this.version = version;
		this.statusDescription = statusDescription;
	}

	@Override
	public byte[] getBody() {
		return body;
	}

	@Override
	public void setBody(byte[] body) {
		this.body = body;
	}

	@Override
	public String getVersion() {
		return version;
	}

	private static HTTPResponse parseResponse(byte[] data) {
		boolean hasData = false;
		String content = new String(data).trim();
		String lines[] = content.split("\n");
		String head = lines[0].trim();
		String args[] = head.split(" ");
		HTTPResponse request = new HTTPResponse(Integer.parseInt(args[1].trim()),
				args[0].trim().toUpperCase().replace("HTTP/", ""), args[2]);
		int split = 0, i = 0;
		String key = "", value = "";
		for (i = 1; i < lines.length; i++) {
			lines[i] = lines[i].trim();
			split = lines[i].indexOf(':');
			if (split == -1 && lines[i].isEmpty()) {
				hasData = true;
				break;
			}
			key = lines[i].substring(0, split).trim();
			value = lines[i].substring(split + 1).trim();
			request.put(key, value);
		}
		return request;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer("HTTP/" + version + " " + statusCode + " " + statusDescription + "\r\n");
		for (Entry<String, Object> entry : this.entrySet()) {
			buffer.append(entry.getKey());
			buffer.append(": ");
			buffer.append(entry.getValue());
			buffer.append("\r\n");
		}
		buffer.append("\r\n");
		if (body != null) {
			buffer.append(new String(body, StandardCharsets.UTF_8));
			buffer.append("\r\n");
		}
		return buffer.toString();
	}

	public static HTTPResponse parseResponse(InputStream in, Charset charset) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(8192);
		boolean dup = false;
		while (true) {
			byte b = (byte) in.read();
			buffer.put(b);
			if (b == 10) {
				if (dup)
					break;
				dup = true;
			} else if (b != 13) {
				dup = false;
			}
		}
		HTTPResponse response = parseResponse(new String(buffer.array(), charset).trim().getBytes(charset));
		if (response.containsKey("Content-Length")) {
			int len = Integer.parseInt(response.get("Content-Length").toString());
			buffer = ByteBuffer.allocate(len);
			while (len-- > 0) {
				buffer.put((byte) in.read());
			}
			response.setBody(buffer.array());
		}
		return response;
	}
	
	@Override
	public byte[] getContent() {
		System.out.println(toString());
		return toString().getBytes(StandardCharsets.UTF_8);
	}

}
