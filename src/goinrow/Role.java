package goinrow;

import java.io.Serializable;
	/**
	 * Player role. EMPTY mean no role defined.*/
	public enum Role implements Serializable {
		HOST(0), EMPTY(1), GUEST(2);
		String[] name= {"O", ".", "X"};
		private final int id;
		private Role(int r){
			id = r;
		}
		public String getName() {
			return name[id];
		}
		public int getValue() {return id;}
	}

