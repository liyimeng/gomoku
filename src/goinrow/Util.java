package goinrow;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class Util {

	public static Map<String, String> parseQuery(String requestLine) {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		try {
			URL url = new URL("http", "0.0.0.0", requestLine);
			String query = url.getQuery();
			if (query != null ) {
				String[] pairs = query.split("&");
				for (String pair : pairs) {
					int idx = pair.indexOf("=");
					query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
							URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
				}	
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException me) {
			me.printStackTrace();
		}
		return query_pairs;
	}
}
