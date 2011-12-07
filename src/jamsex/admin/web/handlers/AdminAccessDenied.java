package jamsex.admin.web.handlers;

import jamsex.admin.web.AdminUrl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;

public class AdminAccessDenied {

	public static void handle(UserService userService, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (userService.isUserLoggedIn() && !userService.isUserAdmin()) {
			resp.setStatus(403);
			resp.getWriter().write("Only registered admins can access this page");
		}
		else {
			resp.sendRedirect(AdminUrl.index.url());
		}	
	}
	
}
