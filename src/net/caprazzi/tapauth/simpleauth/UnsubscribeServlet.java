package net.caprazzi.tapauth.simpleauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import net.caprazzi.tapauth.Misc;
import net.caprazzi.tapauth.SkimpyTemplate;
import net.caprazzi.tapauth.session.PoorSession;
import net.caprazzi.tapauth.session.PoorSessionManager;

public class UnsubscribeServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	
		if (!PoorSessionManager.isLoggedIn(req)) {
			resp.sendRedirect("/simpleauth/login");
			return;
		}
		
		new SkimpyTemplate(getServletContext().getResourceAsStream("/simpleauth/unsubscribe.html"))
		.write(resp.getWriter());
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (!PoorSessionManager.isLoggedIn(req)) {
			resp.sendRedirect("/simpleauth/login");
			return;
		}
		
		if (Misc.isEmpty(req.getParameter("unsubscribe"))) {
			resp.sendRedirect("/simpleauth/unsubscribe");
			return;
		}
		
		PoorSession session = PoorSessionManager.getSession(req);
		Entity currentUser = PoorSessionManager.getCurrentUser(session);
		// wipe user
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.delete(currentUser.getKey());
		
		// logout
		PoorSessionManager.invalidateCurrentSession(req);
		resp.sendRedirect("/simpleauth/login");				
	}
	
}
