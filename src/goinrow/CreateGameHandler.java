package goinrow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.protocol.HttpContext;

public class CreateGameHandler extends GameHandler {
	private final static Logger LOGGER = Logger.getLogger( CreateGameHandler.class.getName());

	private final ConcurrentHashMap<String, Game> allGames;
	public CreateGameHandler(ConcurrentHashMap<String, Game> allGames){
		this.allGames = allGames;
	}
	@Override
	public void handleRequest(final HttpRequest request, final HttpResponse response, final HttpContext context) {
		// TODO Auto-generated method stub
		try {
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
				LOGGER.warning("Invalid board setup");
				redirectTo(response, "/");
				return;
			}
			if (K < 3)
				K = 3;
			if (X < K + 3)
				X = K + 3;

			if (Y < K + 3)
				Y = K + 3;
			Board b = new Board(X, Y, K);
			Game g;
			if (params.get("opponent").equals("computer")) {
				g = new Game(Game.Mode.COMPUTER, b, params.get("desc"));
				allGames.put(g.getID(), g);
				g.setActiveRole(Board.Role.ALICE);
				String url = String.format("/load?g=%s&r=%s&t=%s", g.getID(), 
						Board.Role.ALICE.getRole(), 
						g.getHostToken()) ;
				redirectTo(response, url );
				return;
			} else if (params.get("opponent").equals("private")) {
				g = new Game(Game.Mode.PRIVATE, b, params.get("desc"));
				allGames.put(g.getID(), g);
				g.setActiveRole(Board.Role.BOB);
				String links = String.format("Game created.<a href=/load?g=%s&r=%s&t=%s>Load game</a> <a href=/join?g=%s&r=%s>Invitation</a>",
						g.getID(), Board.Role.ALICE.getRole(), g.getHostToken(),
						g.getID(), Board.Role.BOB.getRole());
				response.setStatusCode(HttpStatus.SC_OK);
				entity = new NStringEntity(String.format(html, links), ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				return;
			} else if (params.get("opponent").equals("public")) {
				g = new Game(Game.Mode.GAMEROOM, b, params.get("desc"));
				allGames.put(g.getID(), g);
				g.setActiveRole(Board.Role.BOB);
				String url = String.format("/load?g=%s&r=%s&t=%s", g.getID(), 
						Board.Role.ALICE.getRole(), 
						g.getHostToken()) ;
				redirectTo(response, url );
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		redirectTo(response, "/");
	}

}
