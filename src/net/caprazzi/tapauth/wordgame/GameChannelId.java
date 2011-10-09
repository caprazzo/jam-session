package net.caprazzi.tapauth.wordgame;

/**
 * Encapsulates information about the current game session.
 * @author dikappa
 *
 */
public class GameChannelId {

	private final String playerId;
	private final String gameId;
	private final String matchId;

	public GameChannelId(String playerId, String gameId, String matchId) {
		this.playerId = playerId;
		this.gameId = gameId;
		this.matchId = matchId;
	}

	public static GameChannelId parse(String channelId) {
		String[] parts = channelId.split(":");
		return new GameChannelId(parts[0], parts[1], parts[2]);
	}

	public static String getChannelId(String gameId, Match match, Player player) {
		return player.getId() + ":" + gameId + ":" + match.getId();
	}
	
	public static String getChannelId(String gameId, String matchId, Player opponent) {
		return opponent.getId() + ":" + gameId + ":" + matchId;
	}

	public String getGameId() {
		return gameId;
	}
	
	public String getMatchId() {
		return matchId;
	}
	
	public String getPlayerId() {
		return playerId;
	}	
	

}
