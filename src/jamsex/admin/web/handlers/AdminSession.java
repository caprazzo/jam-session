package jamsex.admin.web.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

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
			.add("apps", listApps(sessionEntity))
			.add("codes", listQrCodes(sessionEntity))
			.write(info.getResp().getWriter());
	}
	
	private static String listQrCodes(Entity sessionEntity) {
		Query codes_query = new Query("JamQr")
			.addFilter("type", FilterOperator.EQUAL, "session")
			.addFilter("session_id", FilterOperator.EQUAL, sessionEntity.getKey().getId());
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(codes_query);
		
		StringBuilder sb = new StringBuilder();
		for (Entity e : pq.asIterable()) {
			sb.append("<li>")
			.append("<a href=\"/admin/qr_code/" + e.getKey().getId() + "\">" + e.getKey().getId() + "</a>")
			.append("</li>");
		}
		return sb.toString();
	}

	private static String listApps(Entity sessionEntity) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		//Key sessionKey = KeyFactory.createKey("JamSession", id);
		
		/*
		Entity sessionEntity;
		try {
			sessionEntity = datastore.get(sessionKey);
		} catch (EntityNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		*/
		
		// list installed apps
		Query installed = new Query("InstalledApp", sessionEntity.getKey());
		PreparedQuery intalled_pq = datastore.prepare(installed);
		
		HashMap<Long, Long> installed_ids = new HashMap<Long, Long>();
		for (Entity e : intalled_pq.asIterable()) {
			Long appId = (Long) e.getProperty("app_id");
			installed_ids.put(appId, e.getKey().getId());
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
			
			long session_id = sessionEntity.getKey().getId();
			if (installed_ids.containsKey(e.getKey().getId())) {
				Long install_id = installed_ids.get(e.getKey().getId());
				sb.append("<span><strong>Installed</strong></span>");
				sb.append("<span><form action=\"/admin/jam_session/" + session_id + "/install_app\" method=\"POST\">" +
						"<input type=\"hidden\" name=\"action\" value=\"uninstall\"/>" +
						"<input type=\"hidden\" name=\"install_id\" value=\"" + install_id + "\"/>" +
						"<input type=\"submit\" value=\"Uninstall\"/></form></span><br/>");
				sb.append("<a href=\"/admin/jam_session/" + session_id + "/app/" + install_id + "\">Manage installation</a>");
			}
			else {
				sb.append("<span><strong>Not installed</strong></span>");
				sb.append("<span><form action=\"/admin/jam_session/" + session_id + "/install_app\" method=\"POST\">" +
						"<input type=\"hidden\" name=\"action\" value=\"install\"/>" +
						"<input type=\"hidden\" name=\"app_id\" value=\"" + e.getKey().getId() + "\"/>" +
						"<input type=\"submit\" value=\"Install\"/></form></span>");
			}
			
			sb.append("</li>");
		}
		
		return sb.toString();
	}

}
