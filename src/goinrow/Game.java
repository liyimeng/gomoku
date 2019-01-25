package goinrow;

import java.sql.Timestamp;

public class Game implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Board board;
	private Player.Role active;
	private String created;
	private String desc;
	
	public Game(Board b, String desc) {
		board = b; 
		this.desc = desc;
		created =  new Timestamp(System.currentTimeMillis()).toString();
	}
	
	public void setActiveRole(Player.Role r) {
		this.active = r;
	}
	
	public Player.Role getActiveRole() {
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
}
