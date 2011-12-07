package jamsex.admin.web.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import net.caprazzi.tapauth.SkimpyTemplate;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import jamsex.framework.RequestInfo;

public class AdminInstallApp extends AdminPageHandler {

	public static void handle(RequestInfo info) throws IOException {
		
		if (!checkauth(info))
			return;
		
		if (info.isGet())
			handleGet(info);
		
		else if (info.isPost())
			handlePost(info);
		
	}

	private static void handlePost(RequestInfo info) throws IOException {
		long session_id = Long.parseLong(info.get(-2));
		
		String action = info.getReq().getParameter("action");
						
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key sessionKey = KeyFactory.createKey("JamSession", session_id);
		if (action.equals("install")) {		
			long app_id = Long.parseLong(info.getReq().getParameter("app_id"));
			Key appKey = KeyFactory.createKey("JamApp", app_id);
			
			Entity sessionEntity;
			Entity appEntity;
			
			try {
				sessionEntity = datastore.get(sessionKey);
				appEntity = datastore.get(appKey);
			} catch (EntityNotFoundException e1) {
				throw new RuntimeException(e1);
			}
			Entity installEntity = new Entity("InstalledApp", sessionKey);
			installEntity.setProperty("app_id", appKey.getId());
			Key installKey = datastore.put(installEntity);			
		}
		
		else if (action.equals("uninstall")) {
			long install_id = Long.parseLong(info.getReq().getParameter("install_id"));
			Key installKey = KeyFactory.createKey(sessionKey, "InstalledApp", install_id);
			try {
				datastore.get(installKey);
			} catch (EntityNotFoundException e) {
				throw new RuntimeException(e);
			}
			datastore.delete(installKey);
		}
		
		info.getResp().sendRedirect("/admin/jam_session/" + session_id);						
	}

	private static void handleGet(RequestInfo info) throws IOException {
		
		long id = Long.parseLong(info.get(-2));
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key sessionKey = KeyFactory.createKey("JamSession", id);
		
		Entity sessionEntity;
		try {
			sessionEntity = datastore.get(sessionKey);
		} catch (EntityNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		
		// list installed apps
		Query installed = new Query("InstalledApp", sessionKey);
		PreparedQuery intalled_pq = datastore.prepare(installed);
		
		LinkedList<Long> installed_ids = new LinkedList<Long>();
		for (Entity e : intalled_pq.asIterable()) {
			Long appId = (Long) e.getProperty("app_id");
			installed_ids.add(appId);
			System.out.println("Installed: " + appId);
		}
		
		// list all apps
		Query available = new Query("JamApp");
		PreparedQuery available_pq = datastore.prepare(available);
		
		StringBuilder sb = new StringBuilder();
		for (Entity e : available_pq.asIterable()) {
			sb
			.append("<li>")
			.append("<span>name: ").append(e.getProperty("name")).append("</span></br>")
			.append("<span>description: ").append(e.getProperty("desc")).append("</span></br>")
			.append("<span>url: ").append(e.getProperty("url")).append("</span></br>");
			
			if (installed_ids.contains(e.getKey().getId())) {
				sb.append("<span><strong>Installed</strong></installed>");
			}
			else {
				sb.append("<span><form method=\"POST\">" +
						"<input type=\"hidden\" name=\"app_id\" value=\"" + e.getKey().getId() + "\"/>" +
						"<input type=\"submit\" value=\"Install\"/></form></span>");
			}
			
			sb.append("</li>");
		}
		
		InputStream template = info.getServletContext().getResourceAsStream("/jamsex/admin/install_app.html");
		new SkimpyTemplate(template)
			.add("js_name", (String) sessionEntity.getProperty("name"))
			.add("apps", sb.toString())
			.write(info.getResp().getWriter());
	}

	
	
}
