package jamsex.admin.web.handlers;

import jamsex.admin.web.AdminUrl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;

public class AdminLogin {

	public static void handle(UserService userService, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (userService.isUserLoggedIn()) {
			resp.sendRedirect(AdminUrl.index.url());
		}
		else {
			resp.sendRedirect(userService.createLoginURL(AdminUrl.login.url()));
		}
	}
}
