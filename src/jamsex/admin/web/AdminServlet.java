package jamsex.admin.web;

import jamsex.admin.web.handlers.AdminAccessDenied;
import jamsex.admin.web.handlers.AdminApp;
import jamsex.admin.web.handlers.AdminCreateApp;
import jamsex.admin.web.handlers.AdminCreateSession;
import jamsex.admin.web.handlers.AdminCreateSessionQr;
import jamsex.admin.web.handlers.AdminIndex;
import jamsex.admin.web.handlers.AdminInstallApp;
import jamsex.admin.web.handlers.AdminInstalledApp;
import jamsex.admin.web.handlers.AdminListApps;
import jamsex.admin.web.handlers.AdminLogin;
import jamsex.admin.web.handlers.AdminLogout;
import jamsex.admin.web.handlers.AdminQrCode;
import jamsex.admin.web.handlers.AdminSession;
import jamsex.admin.web.handlers.AdminListSessions;
import jamsex.admin.web.handlers.ListQrCodes;
import jamsex.framework.RequestInfo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class AdminServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		AdminUrl.setBase("/admin");
		
		
		UserService userService = UserServiceFactory.getUserService();
		RequestInfo info = RequestInfo.create(getServletContext(), req, resp, userService);
		
		if (info.isGet(AdminUrl.index.url() + "/"))
			resp.sendRedirect(AdminUrl.index.url());
		
		else if (info.isGet(AdminUrl.index.url()))
			AdminIndex.handle(getServletContext(), userService, req, resp);
		
		else if (info.isGet(AdminUrl.login.url()))
			AdminLogin.handle(userService, req, resp);
		
		else if (info.isGet(AdminUrl.logout.url()))
			AdminLogout.handle(userService, req, resp);
		
		else if (info.isGet(AdminUrl.access_denied.url()))
			AdminAccessDenied.handle(userService, req, resp);
		
		else if (info.isPath(AdminUrl.create_session.url()))
			AdminCreateSession.handle(info);
		
		else if (info.isGet("/admin/jam_session/_"))
			AdminSession.handle(info);
		
		else if (info.isPath("/admin/list_sessions"))
			AdminListSessions.handle(info);
		
		else if (info.isPath("/admin/create_app"))
			AdminCreateApp.handle(info);
		
		else if (info.isGet("/admin/app/_"))
			AdminApp.handle(info);
		
		else if (info.isGet("/admin/list_apps"))
			AdminListApps.handle(info);
		
		else if (info.isPath("/admin/jam_session/_/install_app"))
			AdminInstallApp.handle(info);
		
		else if (info.isGet("/admin/jam_session/_/app/_"))
			AdminInstalledApp.handle(info);
		
		else if (info.isPost("/admin/jam_session/_/new_qr"))
			AdminCreateSessionQr.handle(info);
		
		else if (info.isPath("/admin/qr_code/_"))
			AdminQrCode.handle(info);
		
		else if (info.isGet("/admin/list_qr_codes"))
			ListQrCodes.handle(info);				
		
		else
			resp.sendError(404);
	}
	
	

}
