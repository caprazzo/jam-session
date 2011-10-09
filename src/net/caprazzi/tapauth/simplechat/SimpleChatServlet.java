package net.caprazzi.tapauth.simplechat;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.caprazzi.tapauth.ChannelId;
import net.caprazzi.tapauth.Misc;
import net.caprazzi.tapauth.SkimpyTemplate;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

public class SimpleChatServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(SimpleChatServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String[] parts = req.getRequestURI().split("/");		
		String baseUrl = "http://" + req.getServerName() + ":" + req.getServerPort();
		
		if (parts[2].equals("create")) {
			String chatId = Misc.randomString();
			resp.sendRedirect("/simplechat/view/" + chatId);
			return;
		}
		
		if (parts[2].equals("view")) {
			String chatId = parts[3];
			
			String joinUrl = baseUrl + "/simplechat/join/" + chatId;			
			String qrUrl = "https://chart.googleapis.com/chart?chs=500x500&cht=qr&chl=" + joinUrl;
			
			resp.setContentType("text/html");
			new SkimpyTemplate(getServletContext().getResourceAsStream("/simplechat/create.html"))
				.add("JOIN_URL", joinUrl)
				.add("QR_CODE_URL", qrUrl)
				.write(resp.getWriter());
			
			return;
		}	
		
		if (parts[2].equals("join")) {
			String chatId = parts[3];
			ChannelId channelId = SimpleChatChannelsManager.createChannel(chatId);
			resp.sendRedirect("/simplechat/chat/" + channelId.toString());
			return;
		}
		
		if (parts[2].equals("chat")) {						 
			ChannelId channelId = ChannelId.parse(parts[3]);
			
			// open channel
			ChannelService channelService = ChannelServiceFactory.getChannelService();		
			String token = channelService.createChannel(channelId.toString());
				        
	        SimpleChatChannelsManager.addChannelId(channelId);	        		
	        
			String joinUrl = baseUrl + "/simplechat/chat/" + channelId.toString();
			String qrUrl = "https://chart.googleapis.com/chart?chs=100x100&cht=qr&chl=" + joinUrl;						
			
			String postUrl = "/simplechat/post/" + channelId.toString();
			
			// send application page
			resp.setContentType("text/html");
			new SkimpyTemplate(getServletContext().getResourceAsStream("/sencha/index.html"))
				.add("SCREEN_NAME", channelId.getScreenName())
				.add("JOIN_URL", joinUrl)
				.add("POST_URL", postUrl)
				.add("QR_CODE_URL", qrUrl)
				.add("CHANNEL_TOKEN", token)
				.write(resp.getWriter());
			
			return;
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String[] parts = req.getRequestURI().split("/");		
		
		if (parts[2].equals("post")) {
			ChannelId channelId = ChannelId.parse(parts[3]);
			String message = req.getParameter("message");
			// get list of other clients
			ChannelService channelService = ChannelServiceFactory.getChannelService();
			for(String toChannelId : SimpleChatChannelsManager.getChannelIds(channelId.getChatId())) {
				ChannelId chd = ChannelId.parse(toChannelId);
				log.info("deliver msg [" + message +"] from " + channelId.toString() + " to " + toChannelId);
				String jsonMsg = "{ \"nickname\":\"" + channelId.getScreenName() + "\", \"message\":\"" + message + "\"}";
				channelService.sendMessage(new ChannelMessage(toChannelId, jsonMsg));
			}
			
			return;
		}
		
	}
	
}
