package goinrow;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.protocol.HttpContext;

public abstract class  GameHandler implements HttpAsyncRequestHandler<HttpRequest> {
	private final static Logger LOGGER = Logger.getLogger( GameHandler.class.getName());
	@Override
	public void handle(HttpRequest request, HttpAsyncExchange exchange, HttpContext context)
			throws HttpException, IOException {
		final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
		if (!method.equals("GET") && !method.equals("HEAD")) {
			throw new MethodNotSupportedException(method + " method not supported");
		}
		LOGGER.info(Thread.currentThread().getName() + " requested:" + request.getRequestLine().toString());
		HttpResponse response = exchange.getResponse();
		handleRequest(request, response, context);
		LOGGER.info(Thread.currentThread().getName() + " responsed:" + response.getStatusLine().toString());
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
	
	public void redirectTo(final HttpResponse response, String url) {
		response.setStatusCode(HttpStatus.SC_SEE_OTHER);
		response.setHeader("Location", url);
	} 
	
	public Role auth(final Game g, final Map<String, String> params, final HttpResponse response) {
		Role player = null;
		if (Role.HOST.getName().equals(params.get("r"))) {
			if (g.getHostToken().equals(params.get("t")))
				player = Role.HOST;
		}else {
			if (g.getGuestToken().equals(params.get("t")))
				player = Role.GUEST;					
		}
		if (player == null) {
			response.setStatusCode(HttpStatus.SC_FORBIDDEN);
			final NStringEntity entity = new NStringEntity(
					 "<html><body><h1>Not authorized!</h1><a href=/>Try your own game here.</a></body><html>",
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
		}
		return player;
	}
	public abstract void  handleRequest(final HttpRequest request, final HttpResponse response, final HttpContext context);
}

