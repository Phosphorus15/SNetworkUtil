package net.steepout.net.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import net.steepout.net.INetPacket;
import net.steepout.net.INetSocket;
import net.steepout.net.IOQueue;
import net.steepout.net.NetUtil;
import net.steepout.net.Sender;

@Deprecated
public class DependableMulticast extends MulticastBind {

	volatile AtomicInteger uniqueOrder = new AtomicInteger(0);

	volatile AtomicInteger lastOrder = new AtomicInteger(-1);

	volatile Map<Integer, UDPPacket> dataPool = Collections.synchronizedMap(new TreeMap<Integer, UDPPacket>());

	IOQueue queue = new IOQueue(6);

	public DependableMulticast(String ip, int port) throws IOException {
		this(ip, port, true);
	}

	public DependableMulticast(InetSocketAddress address) throws IOException {
		this(address, true);
	}

	public DependableMulticast(String ip, int port, boolean autoAsync) throws IOException {
		this(new InetSocketAddress(ip, port), autoAsync);
	}

	public DependableMulticast(InetSocketAddress address, boolean autoAsync) throws IOException {
		super(address, autoAsync);
		socket = new DatagramSocket() {
			public void send(DatagramPacket packet) throws IOException {
				byte[] data = packet.getData();
				byte[] ndata = new byte[data.length + 10];
				for (int i = 0; i != data.length; i++)
					ndata[i + 10] = data[i];
				ndata[0] = 0x1a;
				ndata[1] = 0x1b;
				byte[] tmp = null;
				tmp = NetUtil.intToBytes(uniqueOrder.getAndIncrement());
				for (int i = 0; i != 4; i++) {
					ndata[i + 2] = tmp[i];
				}
				tmp = NetUtil.getCRC32(data);
				for (int i = 0; i != 4; i++) {
					ndata[i + 6] = tmp[i];
				}
				packet.setData(ndata);
				super.send(packet);
			}
		};
	}

	public void bind(BiConsumer<INetPacket, INetSocket> process) throws IOException {
		super.bind();
		if (socket.getSoTimeout() == 0)
			socket.setSoTimeout(2000);
	}

	public Sender createAsyncSender(byte[] data) {
		return () -> {
			queue.arrangeTask(() -> {
				DatagramPacket packet = new DatagramPacket(data, data.length);
				try {
					socket.send(packet);
				} catch (Exception e) {
					fallBack.accept(e, true);
				}
				return true;
			});
			return true;
		};
	}

	public void close() {
		super.close();
		queue.close(false);
		uniqueOrder.set(0);
		lastOrder.set(-1);
		dataPool.clear();
	}

	@Override
	public void run() {
		int sequence = 0;
		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
		while (!Thread.currentThread().isInterrupted()) {
			try {
				listener.receive(packet);
				if (packet.getLength() < 10)
					throw new IllegalArgumentException("protocol unmatch!");
				byte[] data = packet.getData();
				if (!(data[0] == 0x1a && data[1] == 0x1b))
					throw new IllegalArgumentException("protocol unmatch!");
				byte[] tmp = new byte[4];
				for (int i = 0; i != 4; i++)
					tmp[i] = data[i + 2];
				sequence = NetUtil.bytesToInt(tmp);
				for (int i = 0; i != 4; i++)
					tmp[i] = data[i + 6];
				packet.setData(Arrays.copyOfRange(data, 10, data.length));
				if (!Arrays.equals(tmp, NetUtil.getCRC32(data)))
					throw new SecurityException("CRC access failed");
				if (lastOrder.get() == -1) {
					lastOrder.set(sequence);
				} else if (sequence != lastOrder.get() + 1) {
					dataPool.put(sequence, new UDPPacket(packet));
					continue;
				}
				processor.accept(new UDPPacket(packet), this);
				while (dataPool.containsKey(lastOrder.incrementAndGet())) {

				}
			} catch (Exception e) {
				fallBack.accept(e, false);
			}
		}
	}

}
