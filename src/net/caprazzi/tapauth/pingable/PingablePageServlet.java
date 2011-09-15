package net.caprazzi.tapauth.pingable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.caprazzi.tapauth.Misc;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

/**
 * Create a web page that is pingable using PingPageServlet
 * @author mcaprari
 */
public class PingablePageServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String clientId = new Long(new Date().getTime()).toString();
		
		ChannelService channelService = ChannelServiceFactory.getChannelService();		
		String token = channelService.createChannel(clientId);
		
		InputStream indexIs = getServletContext().getResourceAsStream("/pingable.html");		
		String index = Misc.readToString(indexIs);
		index = index
				.replaceAll("\\{\\{ clientId \\}\\}", clientId)
				.replaceAll("\\{\\{ token \\}\\}", token);

		resp.setContentType("text/html");
		resp.getWriter().write(index);
	}
	
	
}
