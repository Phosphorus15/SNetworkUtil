package net.steepout.net.unicast;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import net.steepout.net.INetPacket;
import net.steepout.net.hosting.TCPPacket;

public class PassiveTCPPacket extends TCPPacket {

	byte[] data;

	InetSocketAddress destination;

	InetSocketAddress source;

	public PassiveTCPPacket(byte[] data, Socket socket) {
		super(data, new InetSocketAddress(socket.getLocalAddress(), socket.getLocalPort()));
		source = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
	}

}