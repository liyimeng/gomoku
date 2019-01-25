package goinrow;

public class Board implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int maxX, maxY, win;
	private Marker[][] boardTable;

	public Board(int maxX, int maxY, int win) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.win = win;
		this.boardTable = new Marker[maxX][maxY];
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				boardTable[x][y] = Marker.EMPTY;
			}
		}
	}

	public boolean play(int x, int y, Marker m) {
		if (boardTable[x][y] == Marker.EMPTY) {
			boardTable[x][y] = m;
			return true;
		}
		return false;
		
	}
	public boolean winAt(int X, int Y) {
		int x = 0, y = 0,  count = 0;
		// Horizon -
		// check forward
		x = X;
		while (x < maxX - 1 && boardTable[X][Y] == boardTable[++x][Y] && count < win) {
			count++;
		}
		if (count >= 5)
			return true;
		// check backward
		x = X;
		while (x > 1 && boardTable[X][Y] == boardTable[--x][Y] && count < win) {
			count++;
		}

		// Vertical |

		// check forward
		y = Y;
		while (y < maxY - 1 && boardTable[X][Y] == boardTable[X][++y] && count < win) {
			count++;
		}
		if (count >= 5)
			return true;
		// check backward
		y = Y;
		while (y > 1 && boardTable[X][Y] == boardTable[X][--y] && count < win) {
			count++;
		}
		if (count >= 5)
			return true;
		// Slash /
		// check forward
		x =X;
		y= Y;
		while (x < maxX - 1 && y > 1 && boardTable[X][Y] == boardTable[++x][--y] && count < win) {
			count++;
		}
		if (count >= 5)
			return true;
		// check backward
		x =X;
		y= Y;
		while (y < maxY - 1 && x > 1 && boardTable[X][Y] == boardTable[--x][++y] && count < win) {
			count++;
		}
		if (count >= 5)
			return true;

		// Back slash \
		// check forward
		x =X;
		y= Y;
		while (x < maxX - 1 && y < maxX - 1 && boardTable[X][Y] == boardTable[++x][++y] && count < win) {
			count++;
		}
		if (count >= 5)
			return true;
		// check backward
		x =X;
		y= Y;
		while (y > 1 && x > 1 && boardTable[X][Y] == boardTable[--x][--y] && count < win) {
			count++;
		}
		if (count >= 5)
			return true;

		return false;
	}

	
	 public String render(Player.Role r, boolean playable) {
		 String html = "<table>";
		 String tmp;
		 for (int y=0; y < maxY; y++) {
			 html += "<tr>";
			 for (int x = 0 ; x < maxX; x++){
				if (this.boardTable[x][y] == Marker.EMPTY && playable) {
					 tmp = String.format("<td><a href=/play?x=%d&y=%d&r=%s>.</a></td>", x, y, r.getRole());
				}else {
					tmp = String.format("<td>%s</td>", boardTable[x][y].getMarker());						 
				}
				html += tmp;
			}
			 html += "</tr>";
		 }
		 html += "</table>";
		 return html;
	 }
	public enum Marker {
		ALICE("O"), BOB("X"), EMPTY(".");

		private String marker;

		Marker(String marker) {
			this.marker = marker;
		}

		public String getMarker() {
			return marker;
		}
	}

}
