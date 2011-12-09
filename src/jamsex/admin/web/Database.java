package jamsex.admin.web;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class Database {

	public static PreparedQuery listQrCodes() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("JamQr");
		return datastore.prepare(query);
	}
	
	public static Entity getQrCode(long qr_id) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key qrKey = KeyFactory.createKey("JamQr", qr_id);
		try {
			return datastore.get(qrKey);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static Entity getSession(Long session_id) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key sessionKey = KeyFactory.createKey("JamSession", session_id);
		try {
			return datastore.get(sessionKey);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void save(Entity entity) {
		DatastoreServiceFactory.getDatastoreService().put(entity);		
	}

}
