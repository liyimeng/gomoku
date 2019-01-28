package goinrow;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpServerConnection;
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
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

/**
 * Simple HTTP/1.1 server based on a non-blocking I/O model and capable
 * of direct channel (zero copy) data transfer.
 */
public class GameServer {
	private final static Logger LOGGER = Logger.getLogger( GameServer.class.getName());
	ConcurrentHashMap<String, Game> gameStore;
	private String docRoot = "./static";
	private String dbFile = "games.db";
	public static void main(final String[] args) throws Exception {
		int port = 8080;
		if (args.length >= 1) {
			port = Integer.parseInt(args[0]);
		}
		
		final GameServer gs = new GameServer();
		//register a shutdown hook for cleaning up
		Runtime.getRuntime().addShutdownHook(new Thread() {//
            public void run() {
               gs.saveGames();
            }
        });
       gs.start( port );
	}
	
	
	public GameServer() {
		// get the global logger to configure it
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.INFO);
		loadGames();
	}
	
	protected void saveGames() {
		LOGGER.info("Save games before jvm shutdown");
		// Saving of object in a file
		try {
			FileOutputStream file;
			file = new FileOutputStream(dbFile);
			ObjectOutputStream out = new ObjectOutputStream(file);
			// Method for serialization of object
			out.writeObject(gameStore);
			out.close();
			file.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
		}
		LOGGER.info("Game saved.");
	}
	
	private void loadGames() {
		LOGGER.info("Load games from disk");
		// Saving of object in a file
		try {
			// Reading the object from a file 
            FileInputStream file = new FileInputStream(dbFile); 
            ObjectInputStream in = new ObjectInputStream(file); 
            // Method for deserialization of object 
            gameStore = (ConcurrentHashMap<String, Game>)in.readObject(); 
            in.close(); 
            file.close(); 

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (gameStore == null ) {
			gameStore = new ConcurrentHashMap<String, Game>();
		}
		LOGGER.info("Games have been loaded");
	}
	
	
	
	public void start(int port) {
		// Create HTTP protocol processing chain
		HttpProcessor httpproc = HttpProcessorBuilder.create().add(new ResponseDate())
				.add(new ResponseServer("Go/1.1")).add(new ResponseContent()).add(new ResponseConnControl()).build();
		// Create request handler registry
		UriHttpAsyncRequestHandlerMapper reqistry = new UriHttpAsyncRequestHandlerMapper();
		// Register the default handler for all URIs
		reqistry.register("/create", new CreateGameHandler(gameStore));
		reqistry.register("/play", new PlayGameHandler(gameStore));
		reqistry.register("/join", new JoinGameHandler(gameStore));
		reqistry.register("/load", new LoadGameHandler(gameStore));
		reqistry.register("/room", new GameRoomHandler(gameStore));
		reqistry.register("/*", new StaticFileHandler(docRoot));
		// Create server-side HTTP protocol handler
		HttpAsyncService protocolHandler = new HttpAsyncService(httpproc, reqistry) {

			@Override
			public void connected(final NHttpServerConnection conn) {
				LOGGER.info(conn + ": connection open");
				super.connected(conn);
			}

			@Override
			public void closed(final NHttpServerConnection conn) {
				LOGGER.info(conn + ": connection closed");
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
		try {
			// Create server-side I/O reactor
			ListeningIOReactor ioReactor = new DefaultListeningIOReactor(config);
			// Listen of the given port
			ioReactor.listen(new InetSocketAddress(port));
			LOGGER.info("Server listens at: " + String.valueOf(port));
			// Ready to go!
			ioReactor.execute(ioEventDispatch);
		}  catch (IOException e) {
			System.err.println("I/O error: " + e.getMessage());
		}
	}
}