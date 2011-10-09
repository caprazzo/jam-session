package net.caprazzi.tapauth.simplechat;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.caprazzi.tapauth.ChannelId;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

public class SimpleChatChannelHandler  extends HttpServlet {

	private static final Logger log = Logger.getLogger(SimpleChatChannelHandler.class.getName());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String[] parts = req.getRequestURI().split("/");
		if (parts.length != 4) {
			resp.sendError(404);
			return;
		}
		
		ChannelService channelService = ChannelServiceFactory.getChannelService();
		ChannelPresence presence = channelService.parsePresence(req);
		String clientId = presence.clientId();
		ChannelId channelId = ChannelId.parse(clientId);
		String chatId = channelId.getChatId();
		
		if (parts[3].equals("connected")) {
			for(String channel : SimpleChatChannelsManager.getChannelIds(chatId)) {
				String jsonMsg = "{ \"nickname\":\"" + channelId.getScreenName() + "\", \"message\":\"[joined ]\"}" ;
				channelService.sendMessage(new ChannelMessage(channel, jsonMsg));
				log.info("Sending join to " + channel + ": " + jsonMsg);
			}
			return;
		}
		
		if (parts[3].equals("disconnected")) {
			SimpleChatChannelsManager.removeChannel(chatId, clientId);			
			for(String channel : SimpleChatChannelsManager.getChannelIds(chatId)) {
				String jsonMsg = "{ \"nickname\":\"" + channelId.getScreenName() + "\", \"message\":\"[left]\"}" ;
				channelService.sendMessage(new ChannelMessage(channel, jsonMsg));
				log.info("Sending left to " + channel + ": " + jsonMsg);
			}
			return;
		}
	}
	
}
