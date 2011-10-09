package net.caprazzi.tapauth;

public class ChannelId {

	private final String chatId;
	private final String userId;
	private final String screenName;

	public ChannelId(String chatId, String userId, String screenName) {
		this.chatId = chatId;
		this.userId = userId;
		this.screenName = screenName;
	}
	
	public String getChatId() {
		return chatId;
	}
	
	public String getScreenName() {
		return screenName;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public static ChannelId parse(String channelId) {
		String[] parts = channelId.split(":");
		return new ChannelId(parts[0], parts[1], parts[2]);
	}
	
	@Override
	public String toString() {
		return chatId + ":" + userId + ":" + screenName;
	}

}
