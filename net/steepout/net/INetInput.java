package net.steepout.net;

import java.util.function.BiConsumer;

public interface INetInput {
	public void setProcesssor(BiConsumer<INetPacket, INetSocket> process);
}
