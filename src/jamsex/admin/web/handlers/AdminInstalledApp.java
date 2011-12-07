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

public class AdminInstalledApp extends AdminPageHandler {

	public static void handle(RequestInfo info) throws IOException {

		if (!checkauth(info))
			return;
		
		if (info.isGet()) 
			handleGet(info);
	}

	private static void handleGet(RequestInfo info) throws IOException {
		
		long session_id = Long.parseLong(info.get(-3));
		long install_id = Long.parseLong(info.get(-1));
		
		Key sessionKey = KeyFactory.createKey("JamSession", session_id);
		Key installKey = KeyFactory.createKey(sessionKey, "InstalledApp", install_id);
		
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Entity sessionEntity;
		Entity installEntity;		
		Entity appEntity;
		try {
			sessionEntity = datastore.get(sessionKey);
			installEntity = datastore.get(installKey);
			Key appKey = KeyFactory.createKey("JamApp", (Long) installEntity.getProperty("app_id"));
			appEntity = datastore.get(appKey);
		} catch (EntityNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		
		InputStream template = info.getServletContext().getResourceAsStream("/jamsex/admin/installed_app.html");
		new SkimpyTemplate(template)
			.add("js_name", (String) sessionEntity.getProperty("name"))
			.add("js_desc", (String) sessionEntity.getProperty("desc"))
			.add("app_name", (String) appEntity.getProperty("name"))
			.add("app_desc", (String) appEntity.getProperty("desc"))
			.write(info.getResp().getWriter());
		
		
	}

}
