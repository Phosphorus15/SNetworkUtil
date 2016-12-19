package net.steepout.net.unicast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.function.BiConsumer;

import net.steepout.net.INetSocket;

public interface UnicastSocket extends INetSocket {
	public void connect(InetSocketAddress address) throws IOException;

	public void setFallback(BiConsumer<Exception, Boolean> consumer);

	public void setSoTimeOut(int time) throws SocketException;

	public OutputStream getStreamOutput();
	
	public void lockOutput();
	
	public void unlockOutput();
}
