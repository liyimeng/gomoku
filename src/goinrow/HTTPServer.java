package goinrow;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.http.ExceptionLogger;
import org.apache.http.HttpConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestHandler;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

/**
 * Embedded HTTP/1.1 file server based on a non-blocking I/O model and capable
 * of direct channel (zero copy) data transfer.
 */
public class HTTPServer {

	public static void main(final String[] args) throws Exception {
		int port = 8080;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		// Create HTTP protocol processing chain
		HttpProcessor httpproc = HttpProcessorBuilder.create().add(new ResponseDate())
				.add(new ResponseServer("Test/1.1")).add(new ResponseContent()).add(new ResponseConnControl()).build();
		// Create request handler registry
		UriHttpAsyncRequestHandlerMapper reqistry = new UriHttpAsyncRequestHandlerMapper();
		// Register the default handler for all URIs
		reqistry.register("/*", new RequestHandler());
		// Create server-side HTTP protocol handler
		HttpAsyncService protocolHandler = new HttpAsyncService(httpproc, reqistry) {

			@Override
			public void connected(final NHttpServerConnection conn) {
				System.out.println(conn + ": connection open");
				super.connected(conn);
			}

			@Override
			public void closed(final NHttpServerConnection conn) {
				System.out.println(conn + ": connection closed");
				super.closed(conn);
			}

		};
		// Create HTTP connection factory
		NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory;

		connFactory = new DefaultNHttpServerConnectionFactory(ConnectionConfig.DEFAULT);
		// Create server-side I/O event dispatch
		IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);
		// Set I/O reactor defaults
		IOReactorConfig config = IOReactorConfig.custom().setIoThreadCount(1).setSoTimeout(3000).setConnectTimeout(3000)
				.build();
		// Create server-side I/O reactor
		ListeningIOReactor ioReactor = new DefaultListeningIOReactor(config);
		try {
			// Listen of the given port
			ioReactor.listen(new InetSocketAddress(port));
			System.out.println("Listen at:" + String.valueOf(port));

			// Ready to go!
			ioReactor.execute(ioEventDispatch);
			
		} catch (InterruptedIOException ex) {
			System.err.println("Interrupted");
		} catch (IOException e) {
			System.err.println("I/O error: " + e.getMessage());
		}
		System.out.println("Shutdown");
	}

	public static class RequestHandler implements HttpAsyncRequestHandler<HttpRequest> {
		private GameServer server;

		public RequestHandler() {
			super();
			server = new GameServer();
		}

		public HttpAsyncRequestConsumer<HttpRequest> processRequest(final HttpRequest request,
				final HttpContext context) {
			// Buffer request content in memory for simplicity
			return new BasicAsyncRequestConsumer();
		}

		public void handle(final HttpRequest request, final HttpAsyncExchange httpexchange, final HttpContext context)
				throws HttpException, IOException {
			final HttpResponse response = httpexchange.getResponse();
			routeRequest(request, response, context);
			httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
		}

		private void routeRequest(final HttpRequest request, final HttpResponse response, final HttpContext context)
				throws HttpException, IOException {

			final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
			if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
				throw new MethodNotSupportedException(method + " method not supported");
			}

			final String target = request.getRequestLine().getUri();
			// Note longer match should check first!
			if (target.startsWith("/create")) {
				this.server.create(request, response, context);
				return;
			} else if (target.startsWith("/game")) {
				this.server.game(request, response, context);
				return;

			} else if (target.startsWith("/room")) {
				this.server.room(request, response, context);
				return;
			} else {
				this.server.staticFile(request, response, context);
			}
		}
	}
}