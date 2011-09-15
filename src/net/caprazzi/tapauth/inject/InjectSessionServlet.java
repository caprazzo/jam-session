package net.caprazzi.tapauth.inject;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class InjectSessionServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String sessionId = req.getParameter("sxId");
		
		HttpSession session = InjectSessionListener.getSession(sessionId);
		
		Map<String, String> parameterMap = req.getParameterMap();
		for(String parm : parameterMap.keySet()) {
			if (!parm.equals("sxId")) {
				session.setAttribute(parm, parameterMap.get(parm));
			}
		}
								
	}
}

