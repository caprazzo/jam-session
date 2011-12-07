package jamsex.admin.web.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

import net.caprazzi.tapauth.SkimpyTemplate;

import jamsex.framework.RequestInfo;

public class AdminCreateApp extends AdminPageHandler {

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
		String url = info.getReq().getParameter("url");
		
		if (name == null || name.trim().length() == 0 
			|| desc == null || desc.trim().length() == 0
			|| url == null || url.trim().length() == 9) {
			throw new RuntimeException("Name, description and url are mandatory");
		}
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity app = new Entity("JamApp");
		app.setProperty("name", name);
		app.setProperty("desc", desc);
		app.setProperty("url", url);
		app.setProperty("created", new Date());
		
		Key appKey = datastore.put(app);
		long id = appKey.getId();
		info.getResp().sendRedirect("/admin/app/" + id);
	}

	private static void handleGet(RequestInfo info) throws IOException {
		InputStream template = info.getServletContext().getResourceAsStream("/jamsex/admin/create_app.html");
		new SkimpyTemplate(template).write(info.getResp().getWriter());		
	}

}
