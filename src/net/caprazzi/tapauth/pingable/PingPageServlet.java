package net.caprazzi.tapauth.pingable;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

/**
 * Ping a page via an external http call
 * @author mcaprari
 */
public class PingPageServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String clientId = req.getParameter("clientId");
		System.out.println("Received new ping for client " + clientId);
		
		ChannelService channelService = ChannelServiceFactory.getChannelService();			
		channelService.sendMessage(new ChannelMessage(clientId, "ping!"));		
	}
}
