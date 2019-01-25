package goinrow;

public class Player implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Role role;
	public Player(Role role) {
		this.role = role; 
	}

	
	public Role getRole() {
		return role;
	}
	
	public enum Role
	{
	    ALICE("A"),
	    BOB("B");
		
	    private String role;
		Role(String role) {
	        this.role = role;
	    }
	    public String getRole() {
	        return role;
	    }
	}

}
