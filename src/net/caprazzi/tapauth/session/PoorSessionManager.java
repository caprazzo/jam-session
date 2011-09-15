package net.caprazzi.tapauth.session;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.caprazzi.tapauth.Misc;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class PoorSessionManager {

	private static final String POOR_SESSION_COOKIE_NAME = "poor_session_id";

	public static boolean isLoggedIn(HttpServletRequest req) {
		Cookie cookie = getSessionCookie(req);
		if (cookie == null)
			return false;
		
		String sessionId = cookie.getValue();
		
		PoorSession session = findSessionById(sessionId);
		if (session == null)
			return false;
		
		if (session.isLoggedIn()) 
			return true;
		
		return false;		
	}
	
	public static PoorSession getSession(HttpServletRequest req) {
		Cookie cookie = getSessionCookie(req);
		if (cookie == null)
			return null;
		
		String sessionId = cookie.getValue();		
		return findSessionById(sessionId);
	}		
	
	public static Entity getCurrentUser(PoorSession session) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();			
		Key userKey = KeyFactory.createKey("User", session.getUserId());
		Query query = new Query("User", userKey);
		PreparedQuery preparedQuery = datastore.prepare(query);
		Entity entity = preparedQuery.asSingleEntity();
		return entity;
	}

	public static void loginUser(HttpServletResponse resp, String userId) {
		String sessionId = Misc.makeSecret();
		PoorSession session = new PoorSession(sessionId, true, userId);	
		saveSession(session);
		resp.addCookie(new Cookie(POOR_SESSION_COOKIE_NAME, sessionId));
	}
	
	public static void invalidateCurrentSession(HttpServletRequest req) {		
		PoorSession session = getSession(req);
		if (session == null)
			return;
			
		deleteSession(session.getId());		
	}		

	public static PoorSession createTempSession(HttpServletResponse resp) {
		String sessionId = Misc.makeSecret();
		PoorSession session = new PoorSession(sessionId, false, null);
		saveSession(session);
		resp.addCookie(new Cookie(POOR_SESSION_COOKIE_NAME, sessionId));
		return session;
	}
	
	public static PoorSession findSessionById(String sessionId) {
		Entity entity = findSessionEntity(sessionId);
		if (entity == null)
			return null;
		return (PoorSession) fromBlob((Blob) entity.getProperty("session-data"));
	}
	
	public static void loginSession(PoorSession session, String userId) {
		session.setUserId(userId);
		session.setLoggedIn();
		saveSession(session);		
	}
	
	
	private static Entity findSessionEntity(String sessionId) {
		Key key = KeyFactory.createKey("_poor_session", sessionId);
		Query query = new Query("_poor_session", key);		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery preparedQuery = datastore.prepare(query);
		Entity e = preparedQuery.asSingleEntity();
		return e;
	}
	
	private static Cookie getSessionCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		
		if (cookies == null)
			return null;
		
		for(Cookie c : cookies) {
			if (c.getName().equals(POOR_SESSION_COOKIE_NAME)) {
				return c;
			}
		}
		return null;
	}
	
	private static void saveSession(PoorSession session) {
		Key key = KeyFactory.createKey("_poor_session", session.getId());
		Entity entity = new Entity(key);
		entity.setProperty("session-data", toBlob(session));
		DatastoreServiceFactory.getDatastoreService().put(entity);
	}
	
	private static void deleteSession(String sessionId) {			
		 DatastoreServiceFactory.getDatastoreService().delete(findSessionEntity(sessionId).getKey());		
	}
	
	private static Blob toBlob(PoorSession session) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutput out;
		try {
			out = new ObjectOutputStream(baos);
			out.writeObject(session);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new Blob(baos.toByteArray());
	}
	
	private static PoorSession fromBlob(Blob sessionData) {
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(new ByteArrayInputStream(sessionData.getBytes()));
			return (PoorSession) in.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}

}
