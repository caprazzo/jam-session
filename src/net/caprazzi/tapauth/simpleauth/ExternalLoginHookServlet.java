package net.caprazzi.tapauth.simpleauth;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.caprazzi.tapauth.session.PoorSession;
import net.caprazzi.tapauth.session.PoorSessionManager;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

public class ExternalLoginHookServlet extends HttpServlet {

	private static final Logger log = Logger.getLogger(ExternalLoginHookServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	
		
		String[] parts = req.getRequestURI().split("/");
		String secret = parts[parts.length - 1];
		String sessionId = parts[parts.length - 2];
		
		PoorSession session = PoorSessionManager.findSessionById(sessionId);
		if (session == null) {
			resp.sendError(404, "Wrong credentials");
			return;
		}
		
		Query query = new Query("User").addFilter("external-secret", FilterOperator.EQUAL, secret);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery preparedQuery = datastore.prepare(query);
		Entity entity = preparedQuery.asSingleEntity();
		
		if (entity == null) {
			resp.sendError(404, "Wrong credentials");			
			return;
		}						
		
		String userId = entity.getProperty("email").toString();
		log.info("Logging in user " + userId + "with session " + sessionId + " and secret " + secret);
		
		PoorSessionManager.loginSession(session, userId);		
		
		// send success signal to the ext login page		
		ChannelService channelService = ChannelServiceFactory.getChannelService();			
		channelService.sendMessage(new ChannelMessage(sessionId, "success"));
		
		resp.getWriter().write("Your browser is now logged in");
	}
	
	
	
}
