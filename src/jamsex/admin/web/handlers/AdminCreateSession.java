package jamsex.admin.web.handlers;

import jamsex.framework.RequestInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import net.caprazzi.tapauth.SkimpyTemplate;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class AdminCreateSession extends AdminPageHandler {

	public static void handle(RequestInfo info) throws IOException {
		
		if (!checkauth(info))
			return;
		
		if (info.isGet())
			handleGet(info);		
		
		else if (info.isPost())
			handlePost(info);
	}

	private static void handlePost(RequestInfo info) throws IOException {
		String name = info.getReq().getParameter("name");
		String desc = info.getReq().getParameter("description");
		
		if (name == null || name.trim().length() == 0 || desc == null || desc.trim().length() == 0) {
			throw new RuntimeException("Name and Description are mandatory");
		}
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity session = new Entity("JamSession");
		session.setProperty("name", name);
		session.setProperty("desc", desc);
		session.setProperty("created", new Date());
		
		Key sessionKey = datastore.put(session);
		long id = sessionKey.getId();
		info.getResp().sendRedirect("/admin/jam_session/" + id);
	}

	private static void handleGet(RequestInfo info) throws IOException {
		InputStream template = info.getServletContext().getResourceAsStream("/jamsex/admin/create_session.html");
		new SkimpyTemplate(template).write(info.getResp().getWriter());
	}
}
