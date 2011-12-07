package jamsex.admin.web.handlers;

import jamsex.admin.web.AdminUrl;
import jamsex.framework.RequestInfo;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;

public class AdminPageHandler {

	protected static boolean checkauth(UserService userService,
			HttpServletResponse resp) throws IOException {
		if (!userService.isUserLoggedIn()) {
			resp.sendRedirect(AdminUrl.login.url());
			return false;
		}
		else if (!userService.isUserAdmin()) {
			resp.sendRedirect(AdminUrl.access_denied.url());
			return false;
		}
		return true;
	}
	
	protected static boolean checkauth(RequestInfo info) throws IOException {
		return checkauth(info.getUserService(), info.getResp());
	}
}
