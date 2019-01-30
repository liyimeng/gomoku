package goinrow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.protocol.HttpContext;

public class PlayGameHandler extends GameHandler {

	private final ConcurrentHashMap<String, Game> allGames;
	public PlayGameHandler(ConcurrentHashMap<String,Game> gameStore){
		this.allGames = gameStore;
	}
	@Override
	public void handleRequest(HttpRequest request, HttpResponse response, HttpContext context) {
		// TODO Auto-generated method stub
		String html = "<html><head> <link rel='stylesheet' href=board.css> </head><body><div align=center>%s <p>%s</p></div></body></html>";
		Map<String, String> params = parseQuery(request);
		Game g = allGames.get(params.get("g"));
		if (g == null) {
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			final NStringEntity entity = new NStringEntity(
					String.format(html, "<h1>Game not found!</h1>", "<a href=/>Home</a>"),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		}
		
		if (g.getBoard().hasWinner()) {
			response.setStatusCode(HttpStatus.SC_OK);
			final NStringEntity entity = new NStringEntity(
					String.format(html, "<h1>Game is Over!</h1>", g.getBoard().render()),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		}
		
		Board.Role player = auth(g, params, response);
		if (player == null )
			return;

		if (g.getActiveRole() != player) {
			redirectTo(response, String.format("/load?g=%s&r=%s&t=%s", g.getID(), player.getRole(), params.get("t")));
			return;
		}
		
		int x=0 , y = 0;
		try {
			x = Integer.parseInt(params.get("x"));
			y = Integer.parseInt(params.get("y"));
			if(g.getBoard().play(x, y, player)){
				if (player == Board.Role.ALICE) {
					g.setActiveRole(Board.Role.BOB);
				} else {
					g.setActiveRole(Board.Role.ALICE);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		if (g.getBoard().hasWinner()) {
			response.setStatusCode(HttpStatus.SC_OK);
			final NStringEntity entity = new NStringEntity(
					String.format(html, "<h1>You win!</h1><a href=/>Try a new game</>", g.getBoard().render()),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		}
		AIPlayer p = g.getAIPlayer();
		if ( p != null) {
			p.runAgainst(x,y);
			g.setActiveRole(Board.Role.ALICE);
		}
		redirectTo(response, String.format("/load?g=%s&r=%s&t=%s", g.getID(), player.getRole(), params.get("t")));
		return;
	}

}
