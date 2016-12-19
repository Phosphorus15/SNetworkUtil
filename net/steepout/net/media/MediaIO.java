package net.steepout.net.media;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import net.steepout.net.Attributes;
import net.steepout.net.INetPacket;
import net.steepout.net.unicast.UnicastSocket;

public class MediaIO {
	public static <T extends INetPacket> T sealPacket(T packet, Attributes attr, Charset... charsets) {
		packet.setContent(attr.toString().getBytes((charsets.length == 0) ? StandardCharsets.UTF_8 : charsets[0]));
		return packet;
	}

	public static <T extends Attributes> T loadFromPacket(INetPacket packet, T attr, Charset... charsets) {
		attr.load(new String(packet.getContent(), (charsets.length == 0) ? StandardCharsets.UTF_8 : charsets[0]));
		return attr;
	}

	public static void streamImage(UnicastSocket socket, RenderedImage im, String formatName) throws IOException {
		socket.lockOutput();
		ImageIO.write(im, formatName, socket.getStreamOutput());
		socket.unlockOutput();
	}

	public static void streamData(UnicastSocket socket, byte[] data) throws IOException {
		socket.lockOutput();
		socket.getStreamOutput().write(data);
		socket.unlockOutput();
	}

}
