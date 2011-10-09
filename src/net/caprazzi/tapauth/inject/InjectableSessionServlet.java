package net.caprazzi.tapauth.inject;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.caprazzi.tapauth.SkimpyTemplate;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

public class InjectableSessionServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		HttpSession session = req.getSession(true);
		String sessionId = session.getId();
		
		ChannelService channelService = ChannelServiceFactory.getChannelService();		
		String token = channelService.createChannel(sessionId);
		
		StringBuilder sessionMap = new StringBuilder();
		Enumeration<String> attributeNames = session.getAttributeNames();
		while(attributeNames.hasMoreElements()) {
			String name = attributeNames.nextElement();
			sessionMap.append(name).append(" => ").append(session.getAttribute(name).toString()).append("<br/>");
		}
									
		resp.setContentType("text/html");
		new SkimpyTemplate(getServletContext().getResourceAsStream("/injectable.html"))
			.add("sessionId", sessionId)
			.add("token", token)
			.add("session-vars", sessionMap.toString())
			.write(resp.getWriter());				
	}
}
