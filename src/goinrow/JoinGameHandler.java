package goinrow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.protocol.HttpContext;
/**Handle Join request from a player.*/
public class JoinGameHandler extends GameHandler {

	private final ConcurrentHashMap<String, Game> allGames;

	public JoinGameHandler(ConcurrentHashMap<String, Game> allGames) {
		this.allGames = allGames;
	}

	@Override
	public void handleRequest(HttpRequest request, HttpResponse response, HttpContext context) {
		Map<String, String> params = parseQuery(request);
		Game g = allGames.get(params.get("g"));
		if (g == null || g.getGuestToken() != null) {
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			final NStringEntity entity = new NStringEntity(
					"<html><body><h1>Game not found, it might already be over or taken!</h1><a href=/room>Back to game room</a></body><html>",
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		}
		redirectTo(response, String.format("/load?g=%s&r=X&t=%s", g.getID(), g.join()));
		return;
	}

}
