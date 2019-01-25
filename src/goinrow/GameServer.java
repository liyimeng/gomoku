package goinrow;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.util.EntityUtils;

public class GameServer {

    private File docRoot;
    private ConcurrentHashMap<Integer, Game> allGames;
	public GameServer() {
        docRoot = new File("./static/");
		// TODO Auto-generated constructor stub
	}

	public void room(final HttpRequest request, final HttpResponse response, final HttpContext context)
			throws HttpException {

		Game g;
		String gTable = "<div align=center><table><tr><th>Description</th><th> Created </th><th> Link </th></tr>" ;
		for (Integer k : allGames.keySet()) {
			g = allGames.get(k);
			gTable += String.format("<tr><td>%s</td><td>%s</td><td> <a href=join?g=%d>Join</a></td></tr>", g.getDesc(), g.getCreated(), k );
		}
		gTable += "</table></div>";
		response.setStatusCode(HttpStatus.SC_OK);
		String html = "<html><head> <link rel='stylesheet' href=home.css></head><body>" + gTable + 
				"</body></html>";
		
		final NStringEntity entity = new NStringEntity(html,
				ContentType.create("text/html", "UTF-8"));
		response.setEntity(entity);
	}

	public void create(final HttpRequest request, final HttpResponse response, final HttpContext context)
			throws HttpException, IOException {
		try {
        System.out.println( request.getRequestLine());
        HttpEntity entity = null;
        /* handle POST data
	       if (request instanceof HttpEntityEnclosingRequest) {
	           entity = ((HttpEntityEnclosingRequest)request).getEntity();
	       }
	       byte[] data;
	       if (entity == null) {
	           data = new byte [0];
		       System.out.println("Receive non data");
	       } else {
	           data = EntityUtils.toByteArray(entity);
	       }
		System.out.println(new String(data)); */
		String html = "<html><head> <link rel='stylesheet' href=board.css></head><body>%s</body></html>";
        Map<String, String> params = Util.parseQuery(request.getRequestLine().toString());
        int X = Integer.parseInt(params.get("X"));
        int Y = Integer.parseInt(params.get("Y"));
        int K = Integer.parseInt(params.get("K"));
        if (X==0 || Y==0 || K==0) {
            System.out.println("invalid board setup");
            response.setStatusCode(HttpStatus.SC_SEE_OTHER);
    			response.setHeader("Location", "/");
    			return;        		
        }
        if (K < 3)
        		K = 3;
        if (X < K + 3) 
        		X = K + 3;

        if (Y < K + 3) 
        		Y = K + 3;
        Board b = new Board(X, Y, K);
		Game g = new Game(b, params.get("desc"));
        if (params.get("opponent").equals("computer")) {
        		g.setActiveRole(Player.Role.ALICE);
        		html  =  String.format(html, b.render(Player.Role.ALICE, true));        		
        }else if (params.get("opponent").equals("private")) {
    			g.setActiveRole(Player.Role.BOB);
    			html  =  String.format(html, b.render(Player.Role.ALICE, false));
        }else if (params.get("opponent").equals("public")) {
			g.setActiveRole(Player.Role.BOB);
			html  = String.format(html, b.render(Player.Role.ALICE, false));
        }else {
            System.out.println("invalid request");
            response.setStatusCode(HttpStatus.SC_SEE_OTHER);
    			response.setHeader("Location", "/");
    			return;
        }
		response.setStatusCode(HttpStatus.SC_OK);
		entity = new NStringEntity(html, ContentType.create("text/html", "UTF-8"));
		response.setEntity(entity);
		}catch (Exception e) {
			e.printStackTrace();
            response.setStatusCode(HttpStatus.SC_SEE_OTHER);
    			response.setHeader("Location", "/");
    			return;
		}
	}

	public void join(final HttpRequest request, final HttpResponse response, final HttpContext context)
			throws HttpException {
		response.setStatusCode(HttpStatus.SC_OK);
		final NStringEntity entity = new NStringEntity("<html><body><h1>Join the game!</h1></body></html>",
				ContentType.create("text/html", "UTF-8"));
		response.setEntity(entity);
	}

	public void run(final HttpRequest request, final HttpResponse response, final HttpContext context)
			throws HttpException {
		response.setStatusCode(HttpStatus.SC_OK);
		final NStringEntity entity = new NStringEntity("<html><body><h1>Play your turn!</h1></body></html>",
				ContentType.create("text/html", "UTF-8"));
		response.setEntity(entity);
	}
	
	public void staticFile(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {

        String target = request.getRequestLine().getUri();
        if (target.equals("") || target.equals("/")) {
        		target = "index.html";
        }
        final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
        if (!file.exists()) {
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            System.out.println("File " + file.getPath() + " not found");

        } else if (!file.canRead() || file.isDirectory()) {
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            System.out.println("Cannot read file " + file.getPath());
        } else {
        	
            final HttpCoreContext coreContext = HttpCoreContext.adapt(context);
            final HttpConnection conn = coreContext.getConnection(HttpConnection.class);
            response.setStatusCode(HttpStatus.SC_OK);
            final NFileEntity body = new NFileEntity(file, ContentType.create("text/html"));
            response.setEntity(body);
            System.out.println(conn + ": serving file " + file.getPath());
        }
    }
}
