package net.steepout.net.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import net.steepout.net.hosting.TCPServer;

public class HTTPServer extends TCPServer {

	public HTTPServer(int port) throws IOException {
		super(port);
	}

	public class HTTPClientInfo extends ClientInfo {
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
				while (!Thread.interrupted()) {
					try {
						HTTPRequest request = HTTPRequest.parseRequest(socket.getInputStream(), StandardCharsets.UTF_8);
						if (request != null) {
							receiver.accept(this, new HTTPPacket(request, socket));
						}
					} catch (Exception e) {
						fallBack.accept(e, this);
					}
				}
			});
			deamon.start();
			return this;
		}
	}

	@SuppressWarnings("deprecation")
	public void sendPacket(HTTPPacket packet) {
		for (ClientInfo info : client) {
			if (info.getAddress().equals(packet.getDestination().getAddress())
					&& info.getPort() == packet.getDestination().getPort()) {
				sendData(packet.getContent(), info);
			}
		}
	}

	@Override
	public void run() {
		Socket socket = null;
		ClientInfo info = null;
		while (!Thread.interrupted()) {
			try {
				socket = server.accept();
				socket.setSoTimeout(socketTimeOut);
				client.add(info = new HTTPClientInfo().setSocket(socket).setTimestamp(System.currentTimeMillis())
						.startDeamonThread());
				joinListener.accept(info);
				socket = null;
			} catch (IOException e) {
				fallBack.accept(e, null);
			}
		}
	}

}
