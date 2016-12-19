package net.steepout.net.unicast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.function.BiConsumer;

import net.steepout.net.INetPacket;
import net.steepout.net.INetSocket;
import net.steepout.net.Sender;

public class NIOSocket implements UnicastSocket, Runnable {

	SocketChannel channel;

	Socket socket;

	BufferingOutputStream output;

	Thread deamon;

	volatile boolean lock;

	class BufferingOutputStream extends OutputStream {

		SocketChannel channel;

		ByteBuffer outBuffer;

		int len = 0;

		public BufferingOutputStream(SocketChannel channel) {
			this.channel = channel;
			outBuffer = ByteBuffer.allocate(1024);
		}

		public synchronized void write(byte b[]) throws IOException {
			super.write(b, 0, b.length);
		}

		public synchronized void write(byte b[], int off, int len) throws IOException {
			super.write(b, off, len);
		}

		public synchronized void flush() throws IOException {
			System.out.println("flush len " + len);
			outBuffer.position(0);
			channel.write((ByteBuffer) outBuffer.limit(len));
			outBuffer.clear();
			len = 0;
		}

		public void close() throws IOException {
			channel.close();
			outBuffer.clear();
		}

		public synchronized void write(int b) throws IOException {
			len++;
			outBuffer.put((byte) b);
			if (outBuffer.position() >= outBuffer.capacity()) {
				flush();
			}
		}
	}

	BiConsumer<INetPacket, INetSocket> processor = (packet, source) -> {
	};

	BiConsumer<Exception, Boolean> fallBack = (exception, send) -> {

	};

	public NIOSocket() {
		socket = new Socket();
	}

	@Override
	public void connect(InetSocketAddress address) throws IOException {
		socket = SocketChannel.open(address).socket();
		channel = socket.getChannel();
		output = new BufferingOutputStream(channel);
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
		channel.close();
		deamon.interrupt();
		socket.close();
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
		ByteBuffer inBuffer = ByteBuffer.allocate(1024);
		int len = 0;
		INetPacket packet = null;
		while (!Thread.interrupted()) {
			try {
				len = channel.read(inBuffer);
				if (len == 0)
					continue;
				if (len == -1)
					throw new IOException("remote host was closed");
				packet = new PassiveTCPPacket(Arrays.copyOfRange(inBuffer.array(), 0, len), socket);
				inBuffer.position(0);
				processor.accept(packet, this);
			} catch (IOException e) {
				fallBack.accept(e, false);
			}
		}
		inBuffer.clear();
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
