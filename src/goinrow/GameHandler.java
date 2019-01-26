package goinrow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;

public abstract class  GameHandler implements HttpAsyncRequestHandler<HttpRequest> {
	@Override
	public void handle(HttpRequest request, HttpAsyncExchange exchange, HttpContext context)
			throws HttpException, IOException {
		System.out.println(Thread.currentThread().getName() + ":" + request.getRequestLine().getMethod());
		final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
		if (!method.equals("GET") && !method.equals("HEAD")) {
			throw new MethodNotSupportedException(method + " method not supported");
		}
		HttpResponse response = exchange.getResponse();
		handleRequest(request, response, context);
		exchange.submitResponse(new BasicAsyncResponseProducer(response));
	}

	@Override
	public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest request, HttpContext context)
			throws HttpException, IOException {
		return new BasicAsyncRequestConsumer();
	}
	
	public Map<String, String> parseQuery(final HttpRequest request) {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		try {
			URL url = new URL("http", "0.0.0.0", request.getRequestLine().getUri());
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
	
	public abstract void  handleRequest(final HttpRequest request, final HttpResponse response, final HttpContext context);
}

