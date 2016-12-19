package net.steepout.net.media;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import net.steepout.net.INetPacket;

public class StreamBuffer {

	ByteBuffer data;

	WritableByteChannel out;

	long size;

	long written = 0;

	public StreamBuffer(int bufsize, long totalsize, WritableByteChannel channel) {
		data = ByteBuffer.allocate(bufsize);
		out = channel;
		size = totalsize;
	}

	public int append(INetPacket packet) throws IOException {
		for (int i = 0; i != packet.getLength(); i++) {
			if (written >= size)
				return i;
			if (data.position() >= data.capacity())
				flush();
			data.put(packet.getContent()[i]);
			written++;
		}
		return -1;
	}

	public void flush() throws IOException {
		out.write((ByteBuffer) data.limit(data.position()));
		data.position(0);
	}
}
