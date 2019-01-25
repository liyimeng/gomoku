package goinrow;

public class Board implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int maxX, maxY, win;
	private Role[][] boardTable;

	public Board(int maxX, int maxY, int win) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.win = win;
		this.boardTable = new Role[maxX][maxY];
		for (int x = 0; x < maxX; x++) {
			for (int y = 0; y < maxY; y++) {
				boardTable[x][y] = Role.EMPTY;
			}
		}
	}

	public boolean play(int x, int y, Role m) {
		if (x < 0 || x > maxX || y < 0 || y > maxY || boardTable[x][y] != Role.EMPTY) {
			return false;
		}
		boardTable[x][y] = m;
		return true;
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

	 public String render() {
		 String html = "<table>";
		 for (int y=0; y < maxY; y++) {
			 html += "<tr>";
			 for (int x = 0 ; x < maxX; x++){
				 html += "<td>" + boardTable[x][y].getRole()+ "</td>";						 
			}
			 html += "</tr>";
		 }
		 html += "</table>";
		 return html;
	 }
	
	 public String renderForPlay(Role r, int gid) {
		 String html = "<table>";
		 String tmp;
		 for (int y=0; y < maxY; y++) {
			 html += "<tr>";
			 for (int x = 0 ; x < maxX; x++){
				if (this.boardTable[x][y] == Role.EMPTY) {
					 tmp = String.format("<td><a href=/game?g=%d&x=%d&y=%d&r=%s>.</a></td>", gid, x, y, r.getRole());
				}else {
					tmp = String.format("<td>%s</td>", boardTable[x][y].getRole());						 
				}
				html += tmp;
			}
			 html += "</tr>";
		 }
		 html += "</table>";
		 return html;
	 }
	public enum Role {
		ALICE("O"), BOB("X"), EMPTY(".");

		private String role;

		Role(String r) {
			this.role = r;
		}

		public String getRole() {
			return role;
		}
	}

}
