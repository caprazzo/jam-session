package net.caprazzi.tapauth.simpleauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.caprazzi.tapauth.session.PoorSessionManager;

public class LogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {	
		PoorSessionManager.invalidateCurrentSession(req);		
		resp.sendRedirect("/simpleauth/login");
	}
}
