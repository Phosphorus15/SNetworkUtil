package net.steepout.net.unicast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.function.BiConsumer;

import net.steepout.net.INetPacket;
import net.steepout.net.INetSocket;
import net.steepout.net.Sender;

public class CommonSocket implements UnicastSocket, Runnable {

	Socket socket;

	SynchronizedOutputStream output;

	Thread deamon;

	volatile boolean lock = false;

	static class SynchronizedOutputStream extends OutputStream {

		OutputStream stream;

		public SynchronizedOutputStream(OutputStream out) {
			stream = out;
		}

		public synchronized void write(byte b[]) throws IOException {
			super.write(b, 0, b.length);
		}

		public synchronized void write(byte b[], int off, int len) throws IOException {
			super.write(b, off, len);
		}

		public void close() throws IOException {
			stream.flush();
			stream.close();
		}

		/**
		 * Try not using this , use stream.write(new byte{}[(byte) b]) instead
		 * 
		 * @param b
		 * @throws IOException
		 */
		@Deprecated
		public synchronized void write(int b) throws IOException {
			stream.write(b);
		}
	}

	BiConsumer<INetPacket, INetSocket> processor = (packet, source) -> {
	};

	BiConsumer<Exception, Boolean> fallBack = (exception, send) -> {

	};

	public CommonSocket() {
		socket = new Socket();
	}

	@Override
	public void connect(InetSocketAddress address) throws IOException {
		socket.connect(address);
		output = new SynchronizedOutputStream(socket.getOutputStream());
		(deamon = new Thread(this)).start();
	}

	@Override
	public void setProcesssor(BiConsumer<INetPacket, INetSocket> process) {
		processor = (process == null) ? (processor) : process;
	}

	@Override
	public void setFallback(BiConsumer<Exception, Boolean> error) {
		fallBack = (error == null) ? fallBack : error;
	}

	@Override
	public void setSoTimeOut(int time) throws SocketException {
		socket.setSoTimeout(time);
	}

	@Override
	public void close() throws IOException {
		socket.close();
		deamon.interrupt();
	}

	@Override
	public Sender createSender(INetPacket data) {
		return () -> {
			try {
				while (lock)
					;
				output.write(data.getContent());
				return true;
			} catch (IOException e) {
				fallBack.accept(e, true);
				return false;
			}
		};
	}

	@Override
	public Sender createAsyncSender(INetPacket data) {
		return () -> {
			new Thread(() -> {
				try {
					while (lock)
						;
					output.write(data.getContent());
				} catch (IOException e) {
					fallBack.accept(e, true);
				}
			}).start();
			return true;
		};
	}

	@Override
	public OutputStream getStreamOutput() {
		return output;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		int len = 0;
		INetPacket packet = null;
		while (!Thread.interrupted()) {
			try {
				len = socket.getInputStream().read(buffer);
				if (len == -1)
					throw new IOException("remote host was closed");
				if (len == 0)
					continue;
				packet = new PassiveTCPPacket(Arrays.copyOfRange(buffer, 0, len), socket);
				processor.accept(packet, this);
			} catch (IOException e) {
				fallBack.accept(e, false);
			}
		}
	}

	@Override
	public void lockOutput() {
		lock = true;
	}

	@Override
	public void unlockOutput() {
		lock = false;
	}

}
