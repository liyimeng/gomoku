package goinrow;

/**
 * Chess board class.
 */
public class Board implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	public final  int maxX, maxY, matchPoint;
	private Role[][] boardTable;
	private boolean hasWinner;
	public Board(int maxX, int maxY, int matchPoint) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.matchPoint = matchPoint;
		this.hasWinner = false;
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
		hasWinner = matchPointAt(x,y);
		return true;
	}
	public boolean hasWinner() {
		return hasWinner;
	}
	private boolean matchPointAt(int X, int Y) {
		int x = 0, y = 0,  count = 1;
		// Horizon -
		// check forward
		x = X;
		while (x < maxX - 1 && boardTable[X][Y] == boardTable[++x][Y] && count < matchPoint) {
			count++;
		}
		// check backward
		x = X;
		while (x > 0 && boardTable[X][Y] == boardTable[--x][Y] && count < matchPoint) {
			count++;
		}
		if (count >= matchPoint)
			return true;
		
		// Vertical |
		count = 1;
		// check forward
		y = Y;
		while (y < maxY - 1 && boardTable[X][Y] == boardTable[X][++y] && count < matchPoint) {
			count++;
		}
		// check backward
		y = Y;
		while (y > 0 && boardTable[X][Y] == boardTable[X][--y] && count < matchPoint) {
			count++;
		}
		if (count >= matchPoint)
			return true;
		// Slash /
		count = 1;
		// check forward
		x =X;
		y= Y;
		while (x < maxX - 1 && y > 0 && boardTable[X][Y] == boardTable[++x][--y] && count < matchPoint) {
			count++;
		}
		// check backward
		x =X;
		y= Y;
		while (y < maxY - 1 && x > 0 && boardTable[X][Y] == boardTable[--x][++y] && count < matchPoint) {
			count++;
		}
		if (count >= matchPoint)
			return true;

		// Back slash \
		count = 1;
		// check forward
		x =X;
		y= Y;
		while (x < maxX - 1 && y < maxX - 1 && boardTable[X][Y] == boardTable[++x][++y] && count < matchPoint) {
			count++;
		}
		// check backward
		x =X;
		y= Y;
		while (y > 0 && x > 0 && boardTable[X][Y] == boardTable[--x][--y] && count < matchPoint) {
			count++;
		}
		
		return (count >= matchPoint);
	}

	 public String render() {
		 String html = "<table>";
		 for (int y=0; y < maxY; y++) {
			 html += "<tr>";
			 for (int x = 0 ; x < maxX; x++){
				 html += "<td>" + boardTable[x][y].getName()+ "</td>";						 
			}
			 html += "</tr>";
		 }
		 html += "</table>";
		 return html;
	 }
	
	 public String renderForPlay(Role r, String gid,String token) {
		 String html = "<table>";
		 String tmp;
		 
		  for (int y=0; y < maxY; y++) {
			 html += "<tr>";
			 for (int x = 0 ; x < maxX; x++){
				if (this.boardTable[x][y] == Role.EMPTY) {
					 tmp = String.format("<td><a href=/play?g=%s&x=%d&y=%d&r=%s&t=%s>%s</a></td>", gid, x, y, r.getName(), token, Role.EMPTY.getName());
				}else {
					tmp = String.format("<td>%s</td>", boardTable[x][y].getName());						 
				}
				html += tmp;
			}
			 html += "</tr>";
		 }
		 html += "</table>";
		 return html;
	 }
}
