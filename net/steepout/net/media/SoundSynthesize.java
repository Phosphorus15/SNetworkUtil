package net.steepout.net.media;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * Invoking baidu synthesize api
 * 
 * @author Phosphorus15
 *
 */
@Deprecated
class SoundSynthesize {

	private static final String apiKey = "RbTH1NVAGhoQ8b4xvlAUTke8";

	private static final String apiSecretKey = "HUdtREF8IcBZQkgygwOLoRCCiZn0VAiI";

	private static final String tokenFetch = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s";

	private static final String resultFetch = "http://tsn.baidu.com/text2audio?tex=%s&lan=%s&tok=%s&ctp=1&cuid=%s&per=%d&vol=%d&spd=%d&pit=%d";

	private String cacheToken;

	/**
	 * Create a speech synthesizer using the Baidu api (use default api keys)
	 * 
	 * @throws IOException
	 *             Failed to contact the api server (or encountered a
	 *             authentication failure)
	 */
	public SoundSynthesize() throws IOException {
		this(apiKey, apiSecretKey);
	}

	/**
	 * Create a speech synthesizer using the Baidu api
	 * 
	 * @param apiKey
	 * @param apiSecretKey
	 * @throws IOException
	 *             Failed to contact the api server (or encountered a
	 *             authentication failure)
	 */
	public SoundSynthesize(String apiKey, String apiSecretKey) throws IOException {
		URL url = new URL(String.format(tokenFetch, apiKey, apiSecretKey));
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String buffer = "", a = "";
		while ((a = reader.readLine()) != null)
			buffer += a + '\n';
		buffer = buffer.substring(0, buffer.length() - 1);
		SimpleJSONAttributes attr = new SimpleJSONAttributes();
		attr.load(buffer);
		cacheToken = attr.get("access_token"); // yup , we just want this XD
		conn.disconnect();
	}

	/**
	 * Returns a input stream which content a 'audio/mp3' mime-type audio
	 * 
	 * @param text
	 * @return
	 * @throws IOException
	 */
	public InputStream generate(String text) throws IOException {
		return generate(text, 0);
	}

	/**
	 * Returns a input stream which content a 'audio/mp3' mime-type audio
	 * 
	 * @param text
	 * @param gender
	 * @return
	 * @throws IOException
	 */
	public InputStream generate(String text, int gender) throws IOException {
		return generate(text, gender, 5, 5, 5);
	}

	/**
	 * Returns a input stream which content a 'audio/mp3' mime-type audio
	 * 
	 * @param text
	 *            - Text content
	 * @param gender
	 *            - Gender of speaker
	 * @param speed
	 *            - Speaking speed
	 * @param volumn
	 *            - Speaking volume
	 * @param pit
	 *            - Speaking pit
	 * @return A input stream contains the mp3 audio
	 * @throws IOException
	 *             Network failure or synthesize failure
	 */
	public InputStream generate(String text, int gender, int speed, int volumn, int pit) throws IOException {
		URL url = new URL(String.format(resultFetch, text, "zh", cacheToken, UUID.randomUUID().toString(), gender,
				volumn, speed, pit));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (conn.getContentType().equalsIgnoreCase("audio/mp3"))
			return conn.getInputStream();
		else
			throw new IOException("Synthensize failed");
	}

	public static void main(String[] args) throws IOException {
		SoundSynthesize s = new SoundSynthesize();
		s.generate("»¬»ü", 0, 5, 5, 5);
	}
}
