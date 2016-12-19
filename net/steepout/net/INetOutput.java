package net.steepout.net;

public interface INetOutput {

	public Sender createSender(INetPacket data);

	public Sender createAsyncSender(INetPacket data);

}
