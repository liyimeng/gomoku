package goinrow;

import java.util.Collections;
import java.util.TreeMap;
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
	final private Marker[][] boardTable;

	public AIPlayer(Board b) {
		this.b = b;
		MatchPointScore = 8 << b.getMatchPoint();
		boardTable = new Marker[b.getMaxX()][b.getMaxY()];
		for (int i = 0; i < b.getMaxX(); i++)
			for (int j = 0; j < b.getMaxY(); j++)
				boardTable[i][j] = Marker.EMPTY;
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
		if (b.play(x, y, Board.Role.BOB))
			boardTable[x][y] = Marker.MYSELF;
		return;
	}

	// Play our turn against opponent, who just play at (x,y)
	public void runAgainst(int x, int y) {
		this.range.reshape(x, y);
		boardTable[x][y] = Marker.OPPONENT;
		Point p = getBestPoint();
		if (b.play(p.x, p.y, Board.Role.BOB))
			boardTable[p.x][p.y] = Marker.MYSELF;
		range.reshape(p.x, p.y);
		return;
	}

	private Point getBestPoint() {
		TreeMap<Integer, Point> scoresMap = new TreeMap<Integer, Point>(Collections.reverseOrder());
		// go through all empty nodes and calculate score for each one of them, return
		// the highest score point in the end.
		for (int x = range.startX; x <= range.endX; x++)
			for (int y = range.startY; y <= range.endY; y++) {
				if (boardTable[x][y] == Marker.EMPTY) {
					int myScore = countScore(x, y, Marker.MYSELF);
					if (myScore >= MatchPointScore) { // Find a match point!
						return new Point(x, y); // Play and win!
					}

					int opScore = countScore(x, y, Marker.OPPONENT);
					if (opScore >= MatchPointScore) { // Find a match point!, but it below to enemy :(
						return new Point(x, y); // play to avoid lost!
					}
					scoresMap.put(myScore + opScore, new Point(x, y));
					LOGGER.info(String.format("myScore %d opScore %d at %d, %d", myScore, opScore, x, y));
				}
			}
		LOGGER.info(String.format("return best point with score %d at %s", scoresMap.firstEntry().getKey(), scoresMap.firstEntry().getValue()));
		return scoresMap.firstEntry().getValue();
	}

	private int countScore(int X, int Y, Marker m) {
		int x = 0, y = 0, count = 1, score = 0;
		boolean fLive, bLive;
		// Horizon -
		// check forward
		x = X;
		fLive = bLive = false;
		while (x < range.endX && m == boardTable[++x][Y])
			count++;
		if (x < b.getMaxX() )
			fLive = (boardTable[x][Y] == Marker.EMPTY);
		// check backward
		x = X;
		while (x > range.startX && m == boardTable[--x][Y])
			count++;
		if (x >= 0)
			bLive = (boardTable[x][Y] == Marker.EMPTY);

		if (count >= b.getMatchPoint())
			return MatchPointScore;
		else {
			if (fLive && bLive) {
				score = 1 << count;
			} else if (fLive || bLive) {
				score = 1 << (count - 1);
			}
		}
		LOGGER.info(String.format("%s(%d, %d): - c=%d s=%d fl=%b, bl=%b", m, X, Y, count, score, fLive, bLive));
		// Vertical |
		count = 1;
		// check forward
		y = Y;
		fLive = bLive = false;
		while (y < range.endY && m == boardTable[X][++y])
			count++;
		if (y < b.getMaxY() )
			fLive = (boardTable[X][y] == Marker.EMPTY);
		// check backward
		y = Y;
		while (y > range.startY && m == boardTable[X][--y])
			count++;
		if (y >= 0)
			bLive = (boardTable[X][y] == Marker.EMPTY);

		if (count >= b.getMatchPoint())
			return MatchPointScore;
		else {
			if (fLive && bLive) {
				score = score + (1 << count);
			} else if (fLive || bLive) {
				score = score + (1 << (count - 1));
			}
		}
		LOGGER.info(String.format("%s(%d, %d): | c=%d s=%d fl=%b, bl=%b", m, X, Y, count, score, fLive, bLive));

		// Slash /
		count = 1;
		// check forward
		x = X;
		y = Y;
		fLive = bLive = false;
		while (x < range.endX && y > range.startY && m == boardTable[++x][--y])
			count++;
		if (x < b.getMaxX()  && y >= 0)
			fLive = (boardTable[x][y] == Marker.EMPTY);
		// check backward
		x = X;
		y = Y;
		while (y < range.endY && x > range.startX && m == boardTable[--x][++y])
			count++;
		if (x >= 0 && y < b.getMaxY() )
			bLive = (boardTable[x][y] == Marker.EMPTY);

		if (count >= b.getMatchPoint())
			return MatchPointScore;
		else {
			if (fLive && bLive) {
				score = score + (1 << count);
			} else if (fLive || bLive) {
				score = score + (1 << (count - 1));
			}
		}
		LOGGER.info(String.format("%s(%d, %d): / c=%d s=%d fl=%b, bl=%b", m, X, Y, count, score, fLive, bLive));

		// Back slash \
		count = 1;
		// check forward
		x = X;
		y = Y;
		fLive = bLive = false;
		while (x < range.endX && y < range.endY && m == boardTable[++x][++y])
			count++;
		if (x < b.getMaxX()  && y < b.getMaxY() )
			fLive = (boardTable[x][y] == Marker.EMPTY);
		// check backward
		x = X;
		y = Y;
		while (y < range.startY && x > range.startX && m == boardTable[--x][--y])
			count++;
		if (x >= 0 && y >= 0)
			bLive = (boardTable[x][y] == Marker.EMPTY);

		if (count >= b.getMatchPoint())
			return MatchPointScore;
		else {
			if (fLive && bLive) {
				score = score + (1 << count);
			} else if (fLive || bLive) {
				score = score + (1 << (count - 1));
			}
		}
		LOGGER.info(String.format("%s(%d, %d): \\ c=%d s=%d fl=%b, bl=%b", m, X, Y, count, score, fLive, bLive));

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

	private enum Marker {
		MYSELF, EMPTY, OPPONENT
	}

}
