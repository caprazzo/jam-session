package jamsex.admin.web.handlers;

import java.io.IOException;
import java.io.InputStream;

import net.caprazzi.tapauth.SkimpyTemplate;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import jamsex.framework.RequestInfo;

public class AdminListSessions extends AdminPageHandler {

	public static void handle(RequestInfo info) throws IOException {
		
		if (!checkauth(info))
			return;
		
		if (info.isGet())
			handleGet(info);
		
	}

	private static void handleGet(RequestInfo info) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("JamSession");
		PreparedQuery preparedQuery = datastore.prepare(query);
		
		StringBuilder sb = new StringBuilder();
		for (Entity e : preparedQuery.asIterable()) {
			sb
				.append("<li>")
				.append("<span>name: ").append(e.getProperty("name")).append("</span></br>")
				.append("<span>description: ").append(e.getProperty("desc")).append("</span></br>")
				.append("<a href=\"/admin/jam_session/" + e.getKey().getId() + "\">admin</a>")
				.append("</li>");
		}
		
		InputStream template = info.getServletContext().getResourceAsStream("/jamsex/admin/list_sessions.html");
		new SkimpyTemplate(template)
			.add("js_sessions", sb.toString())
			.write(info.getResp().getWriter());
	}

}
