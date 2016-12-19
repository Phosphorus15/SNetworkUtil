package net.steepout.net.http;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import net.steepout.net.unicast.PassiveTCPPacket;

public class HTTPPacket extends PassiveTCPPacket {

	HTTPRequest request;

	public HTTPPacket(HTTPRequest request, Socket socket) {
		super(request.toString().getBytes(StandardCharsets.UTF_8), socket);
		this.request = request;
	}

	public HTTPRequest getRequest() {
		return request;
	}

	public void setRequest(HTTPRequest request) {
		this.request = request;
	}

}
