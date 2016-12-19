package net.steepout.net.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.function.BiConsumer;

import net.steepout.net.INetPacket;
import net.steepout.net.INetSocket;
import net.steepout.net.Sender;

public class MulticastBind implements Runnable, INetSocket {

	InetSocketAddress address;

	MulticastSocket listener;

	DatagramSocket socket;

	boolean autoAsync = true;

	Thread asyncReceive;

	BiConsumer<INetPacket, INetSocket> processor = (packet, source) -> {
	};

	BiConsumer<Exception, Boolean> fallBack = (exception, send) -> {

	};

	public MulticastBind(String ip, int port) throws IOException {
		this(ip, port, true);
	}

	public MulticastBind(InetSocketAddress address) throws IOException {
		this(address, true);
	}

	public MulticastBind(String ip, int port, boolean autoAsync) throws IOException {
		this(new InetSocketAddress(ip, port), autoAsync);
	}

	public MulticastBind(InetSocketAddress address, boolean autoAsync) throws IOException {
		if (address == null)
			throw new NullPointerException("Address could not be null !");
		this.address = address;
		this.autoAsync = autoAsync;
		listener = new MulticastSocket(address.getPort());
		socket = new DatagramSocket();
	}

	public void bind() throws IOException {
		this.bind(null);
	}

	public void bind(BiConsumer<INetPacket, INetSocket> process) throws IOException {
		processor = (process == null) ? (processor) : process;
		listener.joinGroup(address.getAddress());
		socket.connect(address);
		asyncReceive = new Thread(this);
		asyncReceive.start();
	}

	public void setProcesssor(BiConsumer<INetPacket, INetSocket> process) {
		processor = (process == null) ? (processor) : process;
	}

	public void setError(BiConsumer<Exception, Boolean> error) {
		fallBack = (error == null) ? fallBack : error;
	}

	public Sender createAsyncSender(INetPacket data) {
		return () -> {
			new Thread(() -> {
				DatagramPacket packet = new DatagramPacket(data.getContent(), data.getLength());
				try {
					socket.send(packet);
				} catch (Exception e) {
					fallBack.accept(e, true);
				}
			}).start();
			return true;
		};
	}

	public Sender createSender(INetPacket data) {
		if (autoAsync) {
			return createAsyncSender(data);
		} else {
			DatagramPacket packet = new DatagramPacket(data.getContent(), data.getLength());
			return () -> {
				try {
					socket.send(packet);
					return true;
				} catch (Exception e) {
					fallBack.accept(e, true);
					return false;
				}
			};
		}
	}

	public void setListenerTimeOut(int time) throws SocketException {
		listener.setSoTimeout(time);
	}

	public void setSenderTimeOut(int time) throws SocketException {
		socket.setSoTimeout(time);
	}

	public void close() {
		asyncReceive.interrupt();
		listener.close();
		socket.close();
	}

	@Override
	public void run() {
		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
		UDPPacket udp = null;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				listener.receive(packet);
				udp = new UDPPacket(packet);
				if (udp.getSource().equals(socket.getLocalSocketAddress()))
					continue;
				udp.setDestination(socket.getLocalSocketAddress());
				processor.accept(udp, this);
			} catch (Exception e) {
				fallBack.accept(e, false);
			}
		}
	}

}
