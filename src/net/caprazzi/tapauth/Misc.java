package net.caprazzi.tapauth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.caprazzi.tapauth.simpleauth.ProfileServlet;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;


import org.mortbay.log.Log;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class Misc {

	private static final Logger log = Logger.getLogger(Misc.class.getName());
	
	public static String resourceAsString(String path) throws IOException {
		System.out.println(path.getClass().getResource(path).getPath());
		InputStream is = path.getClass().getResourceAsStream(path);
		return readToString(is);
	}
	
	public static String readToString(InputStream is) throws IOException {
		final char[] buffer = new char[0x10000];
		StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(is, "UTF-8");
		int read;
		do {
		  read = in.read(buffer, 0, buffer.length);
		  if (read>0) {
		    out.append(buffer, 0, read);
		  }
		} while (read>=0);
		return out.toString();
	}

	public static boolean isEmpty(String name) {
		return (name == null || name.trim().length() == 0);
	}

	public static String makeUserHash(String email, String password) {
		return email + "@@@" + password;
	}


	private static SecureRandom random = new SecureRandom();
	public static String randomString() {
		return new BigInteger(32, random).toString(32);
	}
	
	public static Entity getCurrentUser(HttpServletRequest req) {
		String userId = req.getSession().getAttribute("userId").toString();
		
		Key userKey = KeyFactory.createKey("User", userId);
		Query query = new Query("User", userKey);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery preparedQuery = datastore.prepare(query);
		Entity entity = preparedQuery.asSingleEntity();
		return entity;
	}
	
	public static void listSessions() throws ClassNotFoundException, IOException {
		Query query = new Query("_ah_SESSION");
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery preparedQuery = datastore.prepare(query);
		for(Entity e : preparedQuery.asIterable()) {
			log.info("Entity: " + e + " key:" + e.getKey());
			log.info("id:" + e.getKey().getId() + " name:" + e.getKey().getName());
			
			for(Entry<String, Object> entry : e.getProperties().entrySet()) {
				log.info(" --> Property: " + entry.getKey() + " " + entry.getValue());
			}			
			
			HashMap<String, Object> s = decodeSessionEntity(e);
			log.info("actual attrs: " + dumpSessionAttributes(s));			
		}
	}


	
	public static void storeSession(String sessionId, HashMap<String, Object> data) throws IOException {
		Key key = KeyFactory.createKey("_ah_SESSION", "_ahs" + sessionId);
		Query query = new Query("_ah_SESSION", key);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery preparedQuery = datastore.prepare(query);
		Entity e = preparedQuery.asSingleEntity();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(baos);
		out.writeObject(data);		
		e.setProperty("_values", new Blob(baos.toByteArray()));
		datastore.put(e);	
	}
	
	private static HashMap<String, Object> decodeSessionEntity(Entity e) throws IOException, ClassNotFoundException {
		Blob b = (Blob) e.getProperty("_values");			
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b.getBytes()));
		HashMap<String, Object> s = (HashMap) in.readObject();
		return s;
	}
	
	public static String dumpSessionAttributes(HashMap<String, Object> session) {
		StringBuilder sessionMap = new StringBuilder();
		for(Entry<String, Object> entry : session.entrySet()) {
			sessionMap.append(entry.getKey()).append(" => ").append(entry.getValue().toString()).append("<br/>");
		}
		return sessionMap.toString();
	}
}
