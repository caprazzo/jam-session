package jamsex.admin.web.handlers;


import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.caprazzi.tapauth.SkimpyTemplate;

import com.google.appengine.api.users.UserService;

public class AdminIndex extends AdminPageHandler {

	public static void handle(ServletContext servletContext, UserService userService, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		if (!checkauth(userService, resp))
			return;		
								
		InputStream template = servletContext.getResourceAsStream("/jamsex/admin/index.html");
		new SkimpyTemplate(template)
			.add("nickname", userService.getCurrentUser().getNickname())
			.write(resp.getWriter());		
	}


}
