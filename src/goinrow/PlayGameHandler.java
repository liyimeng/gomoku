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

	private final ConcurrentHashMap<Integer, Game> allGames;
	public PlayGameHandler(ConcurrentHashMap<Integer, Game> allGames){
		this.allGames = allGames;
	}
	@Override
	public void handleRequest(HttpRequest request, HttpResponse response, HttpContext context) {
		// TODO Auto-generated method stub
		String html = "<html><head> <link rel='stylesheet' href=board.css> %s </head><body><div>%s</div>%s</body></html>";
		final String refresh = "<meta http-equiv='Refresh' content=10>";
		Map<String, String> params = parseQuery(request);
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

}
