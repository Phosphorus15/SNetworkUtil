package net.steepout.net;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

public class NetUtil {

	public static int bytesToInt(byte[] b) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.position(0);
		for (int x = 0; x != 4; x++) {
			buffer.put(b[x]);
		}
		buffer.position(0);
		return buffer.asIntBuffer().get();
	}

	public static byte[] intToBytes(int a) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.asIntBuffer().put(0, a);
		return buffer.array();
	}

	public static long bytesToLong(byte[] b) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.position(0);
		for (int x = 0; x != 8; x++) {
			buffer.put(b[x]);
		}
		buffer.position(0);
		return buffer.asLongBuffer().get();
	}

	public static byte[] longToBytes(long a) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.asLongBuffer().put(0, a);
		return buffer.array();
	}

	public static byte[] getCRC32(byte[] data) {
		CRC32 crc = new CRC32();
		crc.update(data);
		return longToBytes(crc.getValue());
	}

	public static void main(String[] args) {
		
	}
}
