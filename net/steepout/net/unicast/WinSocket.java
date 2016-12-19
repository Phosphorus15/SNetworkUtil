package net.steepout.net.unicast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.BiConsumer;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.WORD;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import net.steepout.net.INetPacket;
import net.steepout.net.INetSocket;
import net.steepout.net.Sender;

@Deprecated // too much sensitive api
public class WinSocket implements UnicastSocket {

	static Pointer winSocketInfo = null;

	LongByReference socket = null;

	public static final int AF_INET = 0x2;

	public static final int SOCK_STREAM = 0x1;

	public static final int IPPROTO_TCP = 0x6;

	public static final int WSA_FLAG_OVERLAPPED = 0x1;

	public static interface WinSock extends Library {
		public static WinSock instance = (WinSock) Native.loadLibrary("Ws2_32", WinSock.class);

		public int WSAStartup(WinDef.WORD id, Pointer pointer);

		public LongByReference socket(int af, int type, int protocol);

		public long inet_addr(String address);

		public short htons(short port);

		public PointerByReference gethostbyname(String name);

		public String inet_ntoa(NativeLong data);

		public int connect(LongByReference socket, Pointer addr, int namelen);

		public static class sockaddr_in extends Structure {
			public short sin_family;
			public short sin_port;
			public NativeLong sin_addr;
			public byte sin_zero[];
		}
	}

	private static WORD makeword(byte a, byte b) {
		return new WORD((((int) b) << 8) | a);
	}

	public WinSocket() {
		if (winSocketInfo == null) {
			PointerByReference pointer = new PointerByReference();
			WinSock.instance.WSAStartup(makeword((byte) 0x2, (byte) 0x0), pointer.getPointer());
			winSocketInfo = pointer.getPointer();
			// initialize win-sock framework
		}
		socket = WinSock.instance.socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	}

	@Override
	public void connect(InetSocketAddress ads) throws IOException {
		System.out.println("233");
		WinSock.sockaddr_in address = new WinSock.sockaddr_in();
		address.sin_family = AF_INET;
		address.sin_port = WinSock.instance.htons((short) ads.getPort());
		address.sin_addr = new NativeLong(WinSock.instance.inet_addr(ads.getAddress().getHostAddress()));
		address.sin_zero = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		System.out.println("crash?");
		WinSock.instance.connect(socket, address.getPointer(), 0x10);
	}

	@Override
	public void setProcesssor(BiConsumer<INetPacket, INetSocket> process) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFallback(BiConsumer<Exception, Boolean> consumer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSoTimeOut(int time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public OutputStream getStreamOutput() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		/*new WinSocket();
		ServerSocket socket = new ServerSocket(8080);
		new Thread(() -> {
			try {
				Thread.sleep(3000);
				System.out.println("connect");
				new WinSocket().connect(new InetSocketAddress("127.0.0.1", 8080));
				System.out.println("successful");
				return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
		System.out.println("233");
		socket.accept();
		System.out.println("accept");*/
		Socket socket = new Socket(InetAddress.getByName("www.baidu.com"),80);
		System.out.println(socket.getOutputStream());
	}

	@Override
	public Sender createSender(INetPacket data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Sender createAsyncSender(INetPacket data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void lockOutput() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unlockOutput() {
		// TODO Auto-generated method stub
		
	}

}
