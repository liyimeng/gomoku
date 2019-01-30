package goinrow;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.protocol.HttpContext;

public class GameRoomHandler extends GameHandler {

	private final ConcurrentHashMap<String, Game> allGames;
	public GameRoomHandler(ConcurrentHashMap<String, Game> allGames){
		this.allGames = allGames;
	}
	@Override
	public void handleRequest(final HttpRequest request, final HttpResponse response, final HttpContext context) {
		Game g;
		String gTable = "<div align=center><table><tr><th>Description</th><th> Created </th><th> Link </th></tr>";
		for (String k : allGames.keySet()) {
			g = allGames.get(k);
			if(g.getMode() != Game.Mode.GAMEROOM || g.getGuestToken() != null)
				continue;
			gTable += String.format("<tr><td> %s </td><td> %s </td><td> <a href=/join?g=%s&r=X>Join</a></td></tr>", g.getDesc(),
					g.getCreated(), k);
		}
		gTable += "</table></div>";
		response.setStatusCode(HttpStatus.SC_OK);
		String html = "<html><head> <link rel='stylesheet' href=home.css></head><body><div align=center><h1>Tic-Tac-Toe Public Game Room</h1>" + gTable + "</div></body></html>";

		final NStringEntity entity = new NStringEntity(html, ContentType.create("text/html", "UTF-8"));
		response.setEntity(entity);

	}

}
