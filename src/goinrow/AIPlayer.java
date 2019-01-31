package goinrow;

import java.util.Collections;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AIPlayer implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger LOGGER = Logger.getLogger(AIPlayer.class.getName());
	public static final int ExtendedStep = 1;
	final private int MatchPointScore;
	final private Board b;
	final private SearchRange range;
	final private Player.Role[][] boardTable;

	public AIPlayer(Board b) {
		this.b = b;
		MatchPointScore = 8 << b.getMatchPoint();
		boardTable = new Player.Role[b.getMaxX()][b.getMaxY()];
		for (int i = 0; i < b.getMaxX(); i++)
			for (int j = 0; j < b.getMaxY(); j++)
				boardTable[i][j] = Player.Role.EMPTY;
		range = new SearchRange();

	}

	private class SearchRange implements java.io.Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		boolean inited;
		int startX, startY, endX, endY;

		public SearchRange() {
			inited = false;
		}

		// reshape the SearchRange to make sure it include x, y
		public void reshape(int x, int y) {
			if (inited) {
				if ((x - ExtendedStep) < startX)
					startX = x - ExtendedStep;
				if ((y - ExtendedStep) < startY)
					startY = y - ExtendedStep;
				if ((x + ExtendedStep) > endX)
					endX = x + ExtendedStep;
				if ((y + ExtendedStep) > endY)
					endY = y + ExtendedStep;
			} else {
				inited = true;
				startX = x - ExtendedStep;
				startY = y - ExtendedStep;
				endX = x + ExtendedStep;
				endY = y + ExtendedStep;
			}
			if (startX < 0)
				startX = 0;
			if (startY < 0)
				startY = 0;
			if (endX >= b.getMaxX())
				endX = b.getMaxX() - 1;
			if (endY >= b.getMaxY())
				endY = b.getMaxY() - 1;
			LOGGER.info(String.format("Current range is: startX=%d, startY=%d; endX=%d, endY=%d", startX, startY, endX,
					endY));
		}
	}

	public void runFirst() {
		int x = b.getMaxX() / 2;
		int y = b.getMaxY() / 2;
		this.range.reshape(x, y);
		if (b.play(x, y, Player.Role.GUEST))
			boardTable[x][y] = Player.Role.GUEST;
		return;
	}

	// Play our turn against opponent, who just play at (x,y), note AIPlayer is always play as guest.
	public void runAgainst(int x, int y) {
		this.range.reshape(x, y);
		boardTable[x][y] = Player.Role.HOST;
		Point p = getBestPoint();
		if (b.play(p.x, p.y, Player.Role.GUEST))
			boardTable[p.x][p.y] = Player.Role.GUEST;
		range.reshape(p.x, p.y);
		return;
	}

	private Point getBestPoint() {
		TreeMap<Integer, Point> scoresMap = new TreeMap<Integer, Point>(Collections.reverseOrder());
		LOGGER.info("=================== Start of Analysis ===================");
		// go through all empty nodes and calculate score for each one of them, return
		// the highest score point in the end.
		for (int x = range.startX; x <= range.endX; x++)
			for (int y = range.startY; y <= range.endY; y++) {
				if (boardTable[x][y] == Player.Role.EMPTY) {
					int myScore = countScore(x, y, Player.Role.GUEST);
					if (myScore >= MatchPointScore) { // Find a match point!
						return new Point(x, y); // Play and win!
					}

					int opScore = countScore(x, y, Player.Role.HOST);
					if (opScore >= MatchPointScore) { // Find a match point!, but it below to enemy :(
						return new Point(x, y); // play to avoid lost!
					}
					scoresMap.put(myScore + opScore, new Point(x, y));
					LOGGER.log(Level.FINE, String.format("myScore %d opScore %d at %d, %d", myScore, opScore, x, y));
				}
			}
		LOGGER.info(String.format("return best point with score %d at %s", scoresMap.firstEntry().getKey(), scoresMap.firstEntry().getValue()));
		LOGGER.info("=================== Stop of Analysis ===================");
		return scoresMap.firstEntry().getValue();
	}

	private int countScore(int X, int Y, Player.Role m) {
		int x = 0, y = 0, count = 1, score = 0;
		boolean fLive, bLive;
		// Horizon -
		// check forward
		x = X;
		while (x < range.endX && m == boardTable[++x][Y])
			count++;
		if (X == b.getMaxX() - 1) {
			fLive = false;
		}else {
			fLive = (boardTable[x][Y] == Player.Role.EMPTY);			
		}
		// check backward
		x = X;
		while (x > range.startX && m == boardTable[--x][Y])
			count++;

		if (X == 0) {
			bLive = false;
		}else {
			bLive = (boardTable[x][Y] == Player.Role.EMPTY);			
		}
		
		if (count >= b.getMatchPoint())
			return MatchPointScore;
		else {
			if (fLive && bLive) {
				score = 1 << count;
			} else if (fLive || bLive) {
				score = 1 << (count - 1);
			}
		}
		LOGGER.log(Level.FINE, String.format("%s(%d, %d): -- c=%d s=%d fl=%b, bl=%b", m, X, Y, count, score, fLive, bLive));
		// Vertical |
		count = 1;
		// check forward
		y = Y;
		while (y < range.endY && m == boardTable[X][++y])
			count++;
		if (Y == b.getMaxY() - 1) 
			fLive =false;
		else 
			fLive = (boardTable[X][y] == Player.Role.EMPTY);
		// check backward
		y = Y;
		while (y > range.startY && m == boardTable[X][--y])
			count++;
		if (Y == 0) {
			bLive = false;
		}else {
			bLive = (boardTable[X][y] == Player.Role.EMPTY);			
		}

		if (count >= b.getMatchPoint())
			return MatchPointScore;
		else {
			if (fLive && bLive) {
				score = score + (1 << count);
			} else if (fLive || bLive) {
				score = score + (1 << (count - 1));
			}
		}
		LOGGER.log(Level.FINE, String.format("%s(%d, %d): | c=%d s=%d fl=%b, bl=%b", m, X, Y, count, score, fLive, bLive));

		// Slash /
		count = 1;
		// check forward
		x = X;
		y = Y;
		while (x < range.endX && y > range.startY && m == boardTable[++x][--y])
			count++;
		if (X == b.getMaxX()-1  || Y == 0) {
			fLive = false;
		}else
			fLive = (boardTable[x][y] == Player.Role.EMPTY);
		// check backward
		x = X;
		y = Y;
		while (y < range.endY && x > range.startX && m == boardTable[--x][++y])
			count++;
		if (X == 0 || Y == b.getMaxY() -1 )
			bLive = false;
		else
			bLive = (boardTable[x][y] == Player.Role.EMPTY);

		if (count >= b.getMatchPoint())
			return MatchPointScore;
		else {
			if (fLive && bLive) {
				score = score + (1 << count);
			} else if (fLive || bLive) {
				score = score + (1 << (count - 1));
			}
		}
		LOGGER.log(Level.FINE, String.format("%s(%d, %d): / c=%d s=%d fl=%b, bl=%b", m, X, Y, count, score, fLive, bLive));

		// Back slash \
		count = 1;
		// check forward
		x = X;
		y = Y;
		fLive = bLive = false;
		while (x < range.endX && y < range.endY && m == boardTable[++x][++y])
			count++;
		if (X == b.getMaxX() - 1  ||  Y ==  b.getMaxY() - 1 )
			fLive = false;
		else
			fLive = (boardTable[x][y] == Player.Role.EMPTY);
		// check backward
		x = X;
		y = Y;
		while (y > range.startY && x > range.startX && m == boardTable[--x][--y])
			count++;
		if (X == 0 || Y == 0)
			bLive = false;
		else
			bLive = (boardTable[x][y] == Player.Role.EMPTY);

		if (count >= b.getMatchPoint())
			return MatchPointScore;
		else {
			if (fLive && bLive) {
				score = score + (1 << count);
			} else if (fLive || bLive) {
				score = score + (1 << (count - 1));
			}
		}
		LOGGER.log(Level.FINE, String.format("%s(%d, %d): \\ c=%d s=%d fl=%b, bl=%b", m, X, Y, count, score, fLive, bLive));

		return score;
	}

	public class Point {
		int x, y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return String.format("x=%d,y=%d", x,y);
		}
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Point point = (Point) o;
			return point.x == x && point.y == y;
		}
	}

}
