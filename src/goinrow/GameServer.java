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
		allGames = new ConcurrentHashMap<Integer, Game>();
		// TODO Auto-generated constructor stub
	}

	public void room(final HttpRequest request, final HttpResponse response, final HttpContext context)
			throws HttpException {

		Game g;
		String gTable = "<div align=center><table><tr><th>Description</th><th> Created </th><th> Link </th></tr>";
		for (Integer k : allGames.keySet()) {
			g = allGames.get(k);
			gTable += String.format("<tr><td>%s</td><td>%s</td><td> <a href=/game?g=%d&r=X>Join</a></td></tr>", g.getDesc(),
					g.getCreated(), k);
		}
		gTable += "</table></div>";
		response.setStatusCode(HttpStatus.SC_OK);
		String html = "<html><head> <link rel='stylesheet' href=home.css></head><body>" + gTable + "</body></html>";

		final NStringEntity entity = new NStringEntity(html, ContentType.create("text/html", "UTF-8"));
		response.setEntity(entity);
	}

	public void create(final HttpRequest request, final HttpResponse response, final HttpContext context)
			throws HttpException, IOException {
		try {
			System.out.println(request.getRequestLine().getUri());
			HttpEntity entity = null;
			/*
			 * handle POST data if (request instanceof HttpEntityEnclosingRequest) { entity
			 * = ((HttpEntityEnclosingRequest)request).getEntity(); } byte[] data; if
			 * (entity == null) { data = new byte [0];
			 * System.out.println("Receive non data"); } else { data =
			 * EntityUtils.toByteArray(entity); } System.out.println(new String(data));
			 */
			String html = "<html><head> <link rel='stylesheet' href=board.css></head><body>%s</body></html>";
			Map<String, String> params = Util.parseQuery(request.getRequestLine().getUri());
			int X = Integer.parseInt(params.get("X"));
			int Y = Integer.parseInt(params.get("Y"));
			int K = Integer.parseInt(params.get("K"));
			if (X == 0 || Y == 0 || K == 0) {
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
			allGames.put(g.hashCode(), g);
			if (params.get("opponent").equals("computer")) {
				g.setActiveRole(Board.Role.ALICE);
				response.setStatusCode(HttpStatus.SC_SEE_OTHER);
				response.setHeader("Location", String.format("/game?g=%d&r=%s", g.hashCode(), g.getActiveRole().getRole()));
				return;
			} else if (params.get("opponent").equals("private")) {
				g.setActiveRole(Board.Role.BOB);
				html = String.format(html, String.format("Game created.<a href=/game?g=%d&r=%s>Host</a> <a href=/game?g=%d&r=%s>Opponent</a>",
						g.hashCode(), Board.Role.ALICE.getRole(),
						g.hashCode(), Board.Role.BOB.getRole()));
				response.setStatusCode(HttpStatus.SC_OK);
				entity = new NStringEntity(html, ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				return;
			} else if (params.get("opponent").equals("public")) {
				g.setActiveRole(Board.Role.BOB);
				response.setStatusCode(HttpStatus.SC_SEE_OTHER);
				response.setHeader("Location", "/room");
				return;
			} else {
				System.out.println("invalid request");
				response.setStatusCode(HttpStatus.SC_SEE_OTHER);
				response.setHeader("Location", "/");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatusCode(HttpStatus.SC_SEE_OTHER);
			response.setHeader("Location", "/");
			return;
		}
	}

	public void game(final HttpRequest request, final HttpResponse response, final HttpContext context)
			throws HttpException {

		String html = "<html><head> <link rel='stylesheet' href=board.css> %s </head><body><div>%s</div>%s</body></html>";
		final String refresh = "<meta http-equiv='Refresh' content=10>";
		Map<String, String> params = Util.parseQuery(request.getRequestLine().getUri());
		Game g = null;
		try {
			Integer gid = Integer.parseInt(params.get("g"));
			g = allGames.get(gid);
		} catch (Exception e) {

		}
		if (g == null) {
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			final NStringEntity entity = new NStringEntity(
					String.format(html, "", "<h1>Game not found!</h1>", "<a href=/>Home</a>"),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		}
		
		if (g.getBoard().hasWinner()) {
			response.setStatusCode(HttpStatus.SC_OK);
			final NStringEntity entity = new NStringEntity(
					String.format(html, "", "<h1>Game is Over!</h1>", g.getBoard().render()),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		}
		
		Board.Role player = Board.Role.ALICE;
		if (!player.getRole().equals(params.get("r")))
			player = Board.Role.BOB;

		if (g.getActiveRole() != player) {
			response.setStatusCode(HttpStatus.SC_FORBIDDEN);
			final NStringEntity entity = new NStringEntity(
					String.format(html, refresh, "Please wait for your turn!", g.getBoard().render()),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		}
		
		String msg;
		boolean played = false;
		int x=0 , y = 0;
		try {
			x = Integer.parseInt(params.get("x"));
			y = Integer.parseInt(params.get("y"));
			if(g.getBoard().play(x, y, player)){
				played = true;
				if (player == Board.Role.ALICE) {
					g.setActiveRole(Board.Role.BOB);
				} else {
					g.setActiveRole(Board.Role.ALICE);
				}
				msg = "It is opponent's turn";
			}else {
				msg = "Invalid play!";
			}
		}catch (Exception e) {
			msg = "It's your turn, please GO!";
		}
		if (!played) {
			response.setStatusCode(HttpStatus.SC_FORBIDDEN);
			final NStringEntity entity = new NStringEntity(String.format(html, "", msg, g.getBoard().renderForPlay(player, g.hashCode())),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		}
		
		if (g.getBoard().hasWinner()) {
			response.setStatusCode(HttpStatus.SC_OK);
			final NStringEntity entity = new NStringEntity(
					String.format(html, "", "<h1>You win!</h1>", g.getBoard().render()),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		} else {
			response.setStatusCode(HttpStatus.SC_OK);
			final NStringEntity entity = new NStringEntity(
					String.format(html, refresh, msg, g.getBoard().render()),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		}
	}

	public void staticFile(final HttpRequest request, final HttpResponse response, final HttpContext context)
			throws HttpException, IOException {

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
