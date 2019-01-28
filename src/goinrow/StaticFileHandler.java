package goinrow;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Logger;

import org.apache.http.HttpConnection;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

public class StaticFileHandler extends GameHandler {
	private File docRoot;
	private final static Logger LOGGER = Logger.getLogger( CreateGameHandler.class.getName());
	public StaticFileHandler(String root) {
		docRoot = new File(root);
	}

	@Override
	public void handleRequest(final HttpRequest request, final HttpResponse response, final HttpContext context) {
		// TODO Auto-generated method stub
		String target = request.getRequestLine().getUri();
		if (target.equals("") || target.equals("/")) {
			target = "index.html";
		}
		try {
			File file;
			file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
			if (!file.exists()) {
				response.setStatusCode(HttpStatus.SC_NOT_FOUND);
				LOGGER.info("File " + file.getPath() + " not found");

			} else if (!file.canRead() || file.isDirectory()) {
				response.setStatusCode(HttpStatus.SC_FORBIDDEN);
				LOGGER.info("Cannot read file " + file.getPath());
			} else {

				final HttpCoreContext coreContext = HttpCoreContext.adapt(context);
				final HttpConnection conn = coreContext.getConnection(HttpConnection.class);
				response.setStatusCode(HttpStatus.SC_OK);
				final NFileEntity body = new NFileEntity(file, ContentType.create("text/html"));
				response.setEntity(body);
				LOGGER.info(conn + ": serving file " + file.getPath());
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
