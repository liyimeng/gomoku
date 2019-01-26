package goinrow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.protocol.HttpContext;

public class CreateGameHandler extends GameHandler {

	private final ConcurrentHashMap<Integer, Game> allGames;
	public CreateGameHandler(ConcurrentHashMap<Integer, Game> allGames){
		this.allGames = allGames;
	}
	@Override
	public void handleRequest(final HttpRequest request, final HttpResponse response, final HttpContext context) {
		// TODO Auto-generated method stub
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
			Map<String, String> params = parseQuery(request);
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

}
