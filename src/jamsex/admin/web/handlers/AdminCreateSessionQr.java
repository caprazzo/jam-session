package jamsex.admin.web.handlers;

import jamsex.framework.RequestInfo;

import java.io.IOException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class AdminCreateSessionQr extends AdminPageHandler {

	public static void handle(RequestInfo info) throws IOException {

		if (!checkauth(info))			
			return;
		
		
		if (info.isPost())
			handlePost(info);
		
	}

	private static void handlePost(RequestInfo info) throws IOException {
		Long session_id = Long.parseLong(info.get(-2));
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key sessionKey = KeyFactory.createKey("JamSession", session_id);
				
		try {
			datastore.get(sessionKey);
		} catch (EntityNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		
		Entity qrEntity = new Entity("JamQr");
		qrEntity.setProperty("type", "session");
		qrEntity.setProperty("session_id", session_id);
		Key qrKey = datastore.put(qrEntity);
		info.getResp().sendRedirect("/admin/qr_code/" + qrKey.getId());
	}	

}
