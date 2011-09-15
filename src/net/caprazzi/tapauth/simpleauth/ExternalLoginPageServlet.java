package net.caprazzi.tapauth.simpleauth;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mortbay.log.Log;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

import net.caprazzi.tapauth.Misc;
import net.caprazzi.tapauth.SkimpyTemplate;
import net.caprazzi.tapauth.session.PoorSession;
import net.caprazzi.tapauth.session.PoorSessionManager;

public class ExternalLoginPageServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(ExternalLoginPageServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (PoorSessionManager.isLoggedIn(req)) {
			resp.sendRedirect("/simpleauth/profile");
			return;
		}				
		
		// invalidate any existing session
		PoorSessionManager.invalidateCurrentSession(req);
		PoorSession session = PoorSessionManager.createTempSession(resp);					
		String extSessionId = session.getId();
		
		// open channel
		ChannelService channelService = ChannelServiceFactory.getChannelService();		
		String token = channelService.createChannel(extSessionId);
		
		new SkimpyTemplate(getServletContext().getResourceAsStream("/simpleauth/external-login.html"))
		.add("base-url", req.getServerName() + ":" + req.getServerPort())
		.add("ext-session-id", extSessionId)
		.add("channel-token", token)
		.write(resp.getWriter());
	}
	
}
