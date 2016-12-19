package net.steepout.net.http;

public interface HTTPMessage {
	public byte[] getBody();

	public void setBody(byte[] body);

	public default void append(byte[] line) {
		byte[] origin = getBody();
		byte[] data = new byte[line.length + origin.length];
		for (int i = 0; i != origin.length; i++) {
			data[i] = origin[i];
		}
		for (int i = 0; i != line.length; i++) {
			data[i + origin.length] = line[i];
		}
	}

	public String getVersion();
	
	public byte[] getContent();
}
