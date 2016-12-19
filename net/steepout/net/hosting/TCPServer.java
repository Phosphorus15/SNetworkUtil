package net.steepout.net.hosting;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.steepout.net.INetPacket;
import net.steepout.net.IOQueue;
import net.steepout.net.unicast.PassiveTCPPacket;

public class TCPServer implements Runnable, Closeable {
	protected ServerSocket server;

	protected int port;

	protected int socketTimeOut = 0;

	protected Thread deamon;

	protected List<ClientInfo> client = Collections.synchronizedList(new LinkedList<ClientInfo>());

	protected BiConsumer<Exception, ClientInfo> fallBack = (exception, send) -> {

	};

	protected Consumer<ClientInfo> joinListener = (socket) -> {

	};

	protected BiConsumer<ClientInfo, INetPacket> receiver = (client, packet) -> {

	};

	public class ClientInfo {
		Socket socket;
		InetAddress address;
		int port;
		long timestamp;
		Thread deamon;

		public Socket getSocket() {
			return socket;
		}

		public ClientInfo setSocket(Socket socket) {
			this.socket = socket;
			return this;
		}

		public InetAddress getAddress() {
			return address;
		}

		public ClientInfo setAddress(InetAddress address) {
			this.address = address;
			return this;
		}

		public int getPort() {
			return port;
		}

		public ClientInfo setPort(int port) {
			this.port = port;
			return this;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public ClientInfo setTimestamp(long timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public void kick() throws IOException {
			deamon.interrupt();
			client.remove(this);
			socket.close();
		}

		public ClientInfo startDeamonThread() {
			deamon = new Thread(() -> {
				TCPPacket packet = null;
				byte[] buffer = new byte[1024];
				int len = 0;
				while (!Thread.interrupted()) {
					try {
						len = socket.getInputStream().read(buffer);
						packet = new PassiveTCPPacket(Arrays.copyOfRange(buffer, 0, len), socket);
						receiver.accept(this, packet);
					} catch (Exception e) {
						fallBack.accept(e, this);
					}
				}
			});
			deamon.start();
			return this;
		}
	}

	public TCPServer(int port) throws IOException {
		server = new ServerSocket();
		this.port = port;
	}

	public void start() throws IOException {
		server.bind(new InetSocketAddress("127.0.0.1", port), 50);
		(deamon = new Thread(this)).start();
	}

	public void close() throws IOException {
		deamon.interrupt();
		client.clear();
		server.close();
	}

	public void broadcast(byte[] data) {
		broadcast(data, (socket) -> {
			return true;
		}, false);
	}

	public void broadcast(byte[] data, Predicate<ClientInfo> filter) {
		broadcast(data, filter, false);
	}

	public void scanClients(Consumer<ClientInfo> scanner) {
		for (ClientInfo info : client) {
			scanner.accept(info);
		}
	}

	public void sendPacket(TCPPacket packet) {
		for (ClientInfo info : client) {
			if (info.address.equals(packet.getDestination().getAddress())
					&& info.port == packet.getDestination().getPort()) {
				sendData(packet.getContent(), info);
			}
		}
	}

	@Deprecated
	public void sendData(byte[] data, ClientInfo info) {
		try {
			info.socket.getOutputStream().write(data);
		} catch (IOException e) {
			fallBack.accept(e, info);
		}
	}

	/**
	 * broadcast a segment of message to all 'selected client'
	 * 
	 * @param data
	 *            - Data you want to send
	 * @param filter
	 *            - Client filter
	 * @param useQueue
	 *            - Use queue here is <i>relatively safe</i> and efficient
	 * @see IOQueue
	 */
	@SuppressWarnings("deprecation")
	public void broadcast(byte[] data, Predicate<ClientInfo> filter, boolean useQueue) {
		IOQueue queue = (useQueue) ? new IOQueue() : null;
		for (ClientInfo info : client) {
			if (filter.test(info))
				if (useQueue)
					queue.arrangeTask(() -> {
						sendData(data, info);
						return true;
					});
				else
					sendData(data, info);
		}
		if (useQueue) {
			queue.close(false);
		}
	}

	public void setError(BiConsumer<Exception, ClientInfo> error) {
		fallBack = (error == null) ? fallBack : error;
	}

	public void setJoin(Consumer<ClientInfo> join) {
		joinListener = (join == null) ? joinListener : join;
	}

	public void setReceiver(BiConsumer<ClientInfo, INetPacket> receive) {
		receiver = (receive == null) ? receiver : receive;
	}

	public void setSoTimeOut(int time) throws SocketException {
		server.setSoTimeout(time);
	}

	public void setSocketTimeOut(int time) {
		socketTimeOut = time;
	}

	@Override
	public void run() {
		Socket socket = null;
		ClientInfo info = null;
		while (!Thread.interrupted()) {
			try {
				socket = server.accept();
				socket.setSoTimeout(socketTimeOut);
				client.add(info = new ClientInfo().setSocket(socket).setTimestamp(System.currentTimeMillis())
						.startDeamonThread());
				joinListener.accept(info);
				socket = null;
			} catch (IOException e) {
				fallBack.accept(e, null);
			}
		}
	}

}
