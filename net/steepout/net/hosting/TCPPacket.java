package net.steepout.net.hosting;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import net.steepout.net.INetPacket;

public class TCPPacket implements INetPacket {

	byte[] data;

	InetSocketAddress destination;

	InetSocketAddress source;

	public TCPPacket(byte[] data, InetSocketAddress destination) {
		setContent(data);
		this.destination = destination;
	}

	@Override
	public byte[] getContent() {
		return data;
	}

	@Override
	public void setContent(byte[] data) {
		this.data = data;
	}

	@Override
	public int getLength() {
		return data.length;
	}

	@Override
	public boolean isRemote() {
		return true;
	}

	@Override
	public InetSocketAddress getDestination() {
		return destination;
	}

	@Override
	public InetSocketAddress getSource() {
		return source;
	}

	@Override
	public void setDestination(SocketAddress address) {
		this.destination = (InetSocketAddress) address;
	}

}