package net.steepout.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class HTTPRequest extends HashMap<String, Object> implements HTTPMessage {

	public enum Method {
		GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3921657907556223280L;

	Method method;

	String version;

	String resource;

	byte[] body;

	public HTTPRequest() {
		this("/");
	}

	public HTTPRequest(String resource) {
		this("1.0", resource);
	}

	public HTTPRequest(String version, String resource) {
		this(Method.GET, version, resource);
	}

	public HTTPRequest(Method method, String version, String resource) {
		this.method = method;
		this.version = version;
		this.resource = resource;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	@Override
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	@Override
	public byte[] getBody() {
		return body;
	}

	@Override
	public void setBody(byte[] body) {
		this.body = body;
	}

	public static HTTPRequest parseRequest(byte[] data) {
		boolean hasData = false;
		String content = new String(data).trim();
		String lines[] = content.split("\n");
		String head = lines[0].trim();
		String args[] = head.split(" ");
		HTTPRequest request = new HTTPRequest(Method.valueOf(args[0].toUpperCase()),
				args[2].trim().toUpperCase().replace("HTTP/", ""), args[1]);
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
		if (hasData && (i + 1) < lines.length) {
			if (!lines[i + 1].trim().isEmpty())
				request.setBody(lines[i + 1].getBytes());
		}
		return request;
	}

	public static HTTPRequest parseRequest(InputStream in, Charset charset) throws IOException {
		System.err.println("Waiting for more");
		ByteBuffer buffer = ByteBuffer.allocate(8192);
		boolean dup = false;
		while (true) {
			byte b = (byte) in.read();
			System.err.println("byte read "+b);
			buffer.put(b);
			if (b == 10) {
				if (dup)
					break;
				dup = true;
			} else if (b != 13) {
				dup = false;
			}
		}
		if (buffer.position() <= 6){
			System.err.println("Less than 6 bytes");
			return null;
		}
		return parseRequest(new String(buffer.array(), charset).trim().getBytes(charset));
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(method + " " + resource + " HTTP/" + version + "\r\n");
		for (Entry<String, Object> entry : this.entrySet()) {
			buffer.append(entry.getKey());
			buffer.append(": ");
			buffer.append(entry.getValue());
			buffer.append("\r\n");
		}
		if (body != null) {
			buffer.append("\r\n");
			buffer.append(new String(body));
		}
		buffer.append("\r\n");
		return buffer.toString();
	}

	public static void main(String[] args) throws IOException {
		HTTPServer server = new HTTPServer(8080);
		server.setJoin((client) -> {
			System.out.println(client);
		});
		server.setReceiver((client, packet) -> {
			HTTPRequest request = HTTPRequest.class.cast(HTTPPacket.class.cast(packet).getRequest());
			System.out.println(request);
			HTTPResponse response = new HTTPResponse(200, "1.0", "Connection Established");
			/*
			 * response.put("Content-Length", "3");
			 * response.setBody("233".getBytes());
			 */
			//response.put("WWW-Authenticate", "Basic realm=\"Test\"");
			try {
				client.getSocket().getOutputStream().write(response.getContent());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		server.start();
	}

	@Override
	public byte[] getContent() {
		return toString().getBytes(StandardCharsets.UTF_8);
	}

}
