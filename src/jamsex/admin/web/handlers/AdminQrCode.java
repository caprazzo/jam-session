package jamsex.admin.web.handlers;

import java.io.IOException;
import java.io.InputStream;

import net.caprazzi.tapauth.SkimpyTemplate;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import jamsex.framework.RequestInfo;

public class AdminQrCode extends AdminPageHandler {

	public static void handle(RequestInfo info) throws IOException {

		if (!checkauth(info))
			return;
		
		if (info.isGet())
			handleGet(info);
		
	}

	private static void handleGet(RequestInfo info) throws IOException {
		
		long qr_id = Long.parseLong(info.get(-1));
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key qrKey = KeyFactory.createKey("JamQr", qr_id);
		Entity qrEntity;
		try {
			qrEntity = datastore.get(qrKey);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		String type = (String) qrEntity.getProperty("type");
		if (type.equals("session")) {
			Long session_id = (Long) qrEntity.getProperty("session_id");
			Key sessionKey = KeyFactory.createKey("JamSession", session_id);
			Entity sessionEntity;
			try {
				sessionEntity = datastore.get(sessionKey);
			} catch (EntityNotFoundException e) {
				throw new RuntimeException(e);
			}
			
			String landing_url = "http://jamsedev.appspot.com/" + Long.toString(qr_id, 36);
			String qr_url = "https://chart.googleapis.com/chart?chs=500x500&cht=qr&chl=" + landing_url;
			
			InputStream template = info.getServletContext().getResourceAsStream("/jamsex/admin/qr.html");
			new SkimpyTemplate(template)
				.add("js_name", (String) sessionEntity.getProperty("name"))
				.add("landing_url", landing_url)
				.add("qr_url", qr_url)
				.write(info.getResp().getWriter());		
		}						
	}

}
