package goinrow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.protocol.HttpContext;

public class LoadGameHandler extends GameHandler {

	private final ConcurrentHashMap<String, Game> allGames;

	public LoadGameHandler(ConcurrentHashMap<String, Game> allGames) {
		this.allGames = allGames;
	}

	@Override
	public void handleRequest(HttpRequest request, HttpResponse response, HttpContext context) {
		final String refresh = "<meta http-equiv='Refresh' content=8>";
		String html = "<html><head> <link rel='stylesheet' href=board.css> %s</head><body><div align=center><p>%s</p> %s</div></body></html>";

		Map<String, String> params = parseQuery(request);
		Game g = allGames.get(params.get("g"));
		if (g == null) {
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			final NStringEntity entity = new NStringEntity(
					String.format(html, "", "<h1>Game not found, it might already be over!</h1>", "<a href=/>Home</a>"),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			return;
		}

		Board.Role player = auth(g, params, response);
		if (player == null)
			return;

		if (g.getBoard().hasWinner()) {
			response.setStatusCode(HttpStatus.SC_OK);
			final NStringEntity entity = new NStringEntity(String.format(html, "",
					"<h1>Game is Over!</h1><a href=/>Try new game here.</a>", g.getBoard().render()),
					ContentType.create("text/html", "UTF-8"));
			response.setEntity(entity);
			allGames.remove(params.get("g"));
			return;
		} else {
			if (g.getActiveRole() == player) {
				response.setStatusCode(HttpStatus.SC_OK);
				final NStringEntity entity = new NStringEntity(
						String.format(html, "", "Please play your turn!",
								g.getBoard().renderForPlay(player, g.getID(), params.get("t"))),
						ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				return;
			} else {
				response.setStatusCode(HttpStatus.SC_OK);
				final NStringEntity entity = new NStringEntity(
						String.format(html, refresh, "Please wait for your turn!", g.getBoard().render()),
						ContentType.create("text/html", "UTF-8"));
				response.setEntity(entity);
				return;
			}

		}
	}

}
