package net.steepout.net;

import java.net.SocketAddress;
import java.util.function.Supplier;

public interface INetPacket extends Cloneable {
	public byte[] getContent();

	public void setContent(byte[] data);

	public default int getLength() {
		return getContent().length;
	}

	public boolean isRemote();

	public SocketAddress getDestination();

	public SocketAddress getSource();

	public void setDestination(SocketAddress address);
}
