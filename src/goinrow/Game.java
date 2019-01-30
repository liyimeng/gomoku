package goinrow;

import java.sql.Timestamp;
import java.util.concurrent.ThreadLocalRandom;

public class Game implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Board board;
	private AIPlayer ai;
	private Board.Role active;
	private String created;
	private String desc;
	private Mode mode;
	private String hostToken;
	private String guestToken;
	private String gid;
	public Game(Mode m, Board b, String desc) {
		this.board = b; 
		this.desc = desc;
		this.mode = m;
		this.gid = generateToken();
		this.created =  new Timestamp(System.currentTimeMillis()).toString();
		switch (this.mode) {
		case COMPUTER:
			ai = new AIPlayer(b);
			hostToken = generateToken();
			guestToken = generateToken();
			break;
		case PRIVATE:
		case GAMEROOM:
			hostToken = generateToken();
			guestToken = null;
			break;
		}
	}
	
	public AIPlayer getAIPlayer() {
		return ai;
	}
	private String generateToken() {
		int t = ThreadLocalRandom.current().nextInt(1000000, 10000000);
		return String.valueOf(t);
	}
	
	public String getGuestToken() {
		return guestToken;
	}
	
	public String getHostToken() {
		return hostToken;
	}
	
	public String getID() {
		return gid;
	}
	
	public Mode getMode() {
		return this.mode;
	}
	public String join() {
		guestToken = generateToken();
		return guestToken;
	}
	public void setActiveRole(Board.Role r) {
		this.active = r;
	}
	
	public Board.Role getActiveRole() {
		return active;
	}
	String getDesc() {
		return desc;
	}
	String getCreated() {
		return created;
	}
	
	Board getBoard() {
		return board;
	}
	
	public enum Mode {
		COMPUTER, PRIVATE, GAMEROOM;
	}

}
