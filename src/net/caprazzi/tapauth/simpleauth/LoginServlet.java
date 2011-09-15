package net.caprazzi.tapauth.simpleauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

import net.caprazzi.tapauth.Misc;
import net.caprazzi.tapauth.SkimpyTemplate;
import net.caprazzi.tapauth.session.PoorSessionManager;

public class LoginServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
			
		if (PoorSessionManager.isLoggedIn(req)) {
			resp.sendRedirect("/simpleauth/profile");
			return;
		}
		
		resp.setContentType("text/html");
		new SkimpyTemplate(getServletContext().getResourceAsStream("/simpleauth/login.html"))
		.add("message", "")		
		.write(resp.getWriter());		
	}		
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (PoorSessionManager.isLoggedIn(req)) {
			resp.sendRedirect("/simpleauth/profile");
			return;
		}
		
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		
		if (Misc.isEmpty(email) || Misc.isEmpty(password)) {
			new SkimpyTemplate(getServletContext().getResourceAsStream("/simpleauth/login.html"))
			.add("message", "Please complete the form")		
			.write(resp.getWriter());
			return;
		}
				
		Key userKey = KeyFactory.createKey("User", email);
		Query query = new Query("User", userKey)
			.addFilter("hash", FilterOperator.EQUAL, Misc.makeUserHash(email, password));
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery preparedQuery = datastore.prepare(query);
		Entity entity = preparedQuery.asSingleEntity();
		
		if (entity == null) {
			new SkimpyTemplate(getServletContext().getResourceAsStream("/simpleauth/login.html"))
			.add("message", "Invalid Credentials")		
			.write(resp.getWriter());
			return;			
		}
		
		PoorSessionManager.loginUser(resp, email);
		resp.sendRedirect("/simpleauth/profile");
		System.out.println("logged in");
	}
	
}
