package net.steepout.net;

public interface Attributes extends java.util.Map<String, String> {
	public void load(String data);

	public static <O extends Attributes, T extends Attributes> T copyTo(O source, T target) {
		source.forEach((key, value) -> {
			target.put(key, value);
		});
		return target;
	}
}
