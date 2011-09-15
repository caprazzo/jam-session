package net.caprazzi.tapauth.simpleauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import net.caprazzi.tapauth.Misc;
import net.caprazzi.tapauth.SkimpyTemplate;

public class RegisterServlet extends HttpServlet {

	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String name = req.getParameter("name");
		String email = req.getParameter("email");
		String password1 = req.getParameter("pw1");
		String password2 = req.getParameter("pw2");
		
		resp.setContentType("text/html");
		if (!Misc.isEmpty(name) 
				&& !Misc.isEmpty(email) 
				&& !Misc.isEmpty(password1)
				&& password1.equals(password2)) {
			
			Key userKey = KeyFactory.createKey("User", email);			
			Entity user = new Entity("User", userKey);
			user.setProperty("email", email);
			user.setProperty("hash", Misc.makeUserHash(email, password1));
			user.setProperty("external-secret", Misc.makeSecret());
			user.setProperty("name", name);
			
			DatastoreServiceFactory.getDatastoreService().put(user);			
			
			new SkimpyTemplate(getServletContext().getResourceAsStream("/simpleauth/register-ok.html"))
			.write(resp.getWriter());
		}
		else {
			new SkimpyTemplate(getServletContext().getResourceAsStream("/simpleauth/register.html"))
			.add("message", "please complete the form")
			.write(resp.getWriter());
		}
				
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException ,IOException {
		
		new SkimpyTemplate(getServletContext().getResourceAsStream("/simpleauth/register.html"))
		.add("message", "")
		.write(resp.getWriter());		
	};
	
}
