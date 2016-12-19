package net.steepout.net.multicast;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.steepout.net.INetPacket;

public class UDPPacket implements INetPacket {

	byte[] data;

	InetSocketAddress destination;

	InetSocketAddress source;

	public UDPPacket(DatagramPacket packet) {
		this.data = packet.getData();
		packet.getSocketAddress();
		destination = new InetSocketAddress(packet.getAddress(), packet.getPort());
		source = (InetSocketAddress) packet.getSocketAddress();
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