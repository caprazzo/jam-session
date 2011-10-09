package net.caprazzi.tapauth.wordgame;

import com.google.appengine.api.datastore.Entity;

public class Player {

	public enum Status {
		NOT_CONNECTED, JOINED, CONNECTED
	}
	
	private String playerId;
	private Status status;

	public Player(Entity playerEntity) {
		this.playerId = playerEntity.getKey().getName();
		this.status = (Status) Enum.valueOf(Status.class, playerEntity.getProperty("STATUS").toString()); 
	}

	public String getId() {
		return playerId;
	}
	
	public static Player fromEntity(Entity playerEntity) {
		return new Player(playerEntity);
	}

	public void setConnected() {
		this.status = Status.CONNECTED;
	}
	
	public Status getStatus() {
		return this.status;
	}

}
