package net.caprazzi.tapauth.session;

import java.io.Serializable;

public class PoorSession implements Serializable {

	private static final long serialVersionUID = 2793718615890430037L;
	private boolean isLoggedIn;
	private String userId;
	private final String sessionId;

	public PoorSession(String sessionId, boolean isLoggedIn, String userId) {
		this.sessionId = sessionId;
		this.isLoggedIn = isLoggedIn;
		this.userId = userId;
	}
	
	public String getId() {
		return sessionId;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}	

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) { 
		this.userId = userId;		
	}

	public void setLoggedIn() {
		this.isLoggedIn = true;
	}
	
}
