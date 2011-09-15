package net.caprazzi.tapauth.simpleauth;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.caprazzi.tapauth.SkimpyTemplate;
import net.caprazzi.tapauth.session.PoorSession;
import net.caprazzi.tapauth.session.PoorSessionManager;

import com.google.appengine.api.datastore.Entity;

public class ProfileServlet extends HttpServlet {

	 private static final Logger log = Logger.getLogger(ProfileServlet.class.getName());
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
								
		if (!PoorSessionManager.isLoggedIn(req)) {
			resp.sendRedirect("/simpleauth/login");
			return;
		}
		
		PoorSession session = PoorSessionManager.getSession(req);
		Entity entity = PoorSessionManager.getCurrentUser(session);
		
		String name = entity.getProperty("name").toString();
		String secret = entity.getProperty("external-secret").toString();
		
		resp.setContentType("text/html");
		new SkimpyTemplate(getServletContext().getResourceAsStream("/simpleauth/profile.html"))
		.add("name", name)
		.add("external-secret", secret)
		.write(resp.getWriter());
		
	}

	
}
