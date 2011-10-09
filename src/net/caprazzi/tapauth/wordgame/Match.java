package net.caprazzi.tapauth.wordgame;

import com.google.appengine.api.datastore.Entity;

public class Match {

	private final Player player;
	private final Player opponent;
	private String matchId;
	private String words;

	public Match(Entity matchEntity, Player player, Player opponent) {
		this.matchId = matchEntity.getKey().getName();
		this.words = matchEntity.getProperty("WORDS").toString();
		this.player = player;
		this.opponent = opponent;
	}
	

	public static Match fromEntities(Entity matchEntity, Entity playerEntity,
			Entity opponentEntity) {				
		
		Player player = Player.fromEntity(playerEntity);
		if (opponentEntity != null)
			return new Match(matchEntity, player, Player.fromEntity(opponentEntity)); 
		
		return new Match(matchEntity, player, null);  
	}

	public String getId() {
		return matchId;
	}

	public Player getPlayer() {
		return player;
	}

	public static Match fromDb(Entity matchEntity, Player player, Player opponent) {
		return new Match(matchEntity, player, opponent);
	}

	public String getWords() {
		return words;
	}

	public Player getOpponent() {
		return opponent;
	}
	

}
