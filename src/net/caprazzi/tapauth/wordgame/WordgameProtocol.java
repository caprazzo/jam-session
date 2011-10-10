package net.caprazzi.tapauth.wordgame;

public class WordgameProtocol {

	public static String notifyOpponentConnected(Player opponent) {
		return "{\"cmd\":\"opponent-connected\"}";
	}

	public static String notifyOpponentStarted() {
		return "{\"cmd\":\"opponent-started\"}";
	}

	public static String notifyParted() {
		return "{\"cmd\":\"parted\"}";
	}

	public static String notifyOpponentWin() {
		return "{\"cmd\":\"opponent-win\"}";
	}

	public static String notifyPlayerWin() {
		return "{\"cmd\":\"player-win\"}";
	}

	public static String notifyOpponentProgress(String progress) {
		return "{\"cmd\":\"opponent-update-progress\", \"progress\":" + progress + "}";
	}

	public static String notifyOpponentRequestReplay() {
		return "{\"cmd\":\"opponent-request-replay\"}";
	}

	public static String notifyRedirect(String location) {
		return "{\"cmd\":\"redirect\", \"location\":\""+location+"\"}";
	}

}
