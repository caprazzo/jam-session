package jamsex.admin.web.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import net.caprazzi.tapauth.SkimpyTemplate;

import jamsex.framework.RequestInfo;

public class AdminSession extends AdminPageHandler {

	public static void handle(RequestInfo info) throws IOException {
		
		if (!checkauth(info))
			return;
		
		if (info.isGet())
			handleGet(info);
		
	}

	private static void handleGet(RequestInfo info) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		long id = Long.parseLong(info.get(-1));
		Key sessionKey = KeyFactory.createKey("JamSession", id);
		Entity sessionEntity;
		try {
			sessionEntity = datastore.get(sessionKey);
		} catch (EntityNotFoundException e) {
			throw new RuntimeException(e);
		}
		InputStream template = info.getServletContext().getResourceAsStream("/jamsex/admin/admin_session.html");
		new SkimpyTemplate(template)
			.add("js_name", (String) sessionEntity.getProperty("name"))
			.add("js_desc", (String)sessionEntity.getProperty("desc"))
			.add("js_id", Long.toString(sessionKey.getId()))
			.write(info.getResp().getWriter());
	}

}
