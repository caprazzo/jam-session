package net.caprazzi.tapauth.inject;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Keep a list of active sessions so sessions can be retrieved by id
 * @author mcaprari
 */
public class InjectSessionListener implements HttpSessionListener  {

	private static final ConcurrentHashMap<String, HttpSession> sessions 
		= new ConcurrentHashMap<String, HttpSession>();
	
	public static HttpSession getSession(String sessionId) {
		return sessions.get(sessionId);
	}
	
	
	@Override
	public void sessionCreated(HttpSessionEvent ev) {
		sessions.put(ev.getSession().getId(), ev.getSession());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent ev) {
		sessions.remove(ev.getSession().getId());		
	}

	

}
