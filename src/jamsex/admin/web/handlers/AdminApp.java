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

public class AdminApp extends AdminPageHandler {

	public static void handle(RequestInfo info) throws IOException {
		
		if (!checkauth(info))
			return;
		
		if (info.isGet()) 
			handleGet(info);
		
	}

	private static void handleGet(RequestInfo info) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		long id = Long.parseLong(info.get(-1));
		Key key = KeyFactory.createKey("JamApp", id);
		Entity appEntity;
		try {
			appEntity = datastore.get(key);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e);
		}
		InputStream template = info.getServletContext().getResourceAsStream("/jamsex/admin/admin_app.html");
		new SkimpyTemplate(template)
			.add("js_name", (String) appEntity.getProperty("name"))
			.add("js_desc", (String)appEntity.getProperty("desc"))
			.add("js_url", (String)appEntity.getProperty("url"))
			.write(info.getResp().getWriter());
	}

}
