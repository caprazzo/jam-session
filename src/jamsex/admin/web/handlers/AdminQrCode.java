package jamsex.admin.web.handlers;

import jamsex.admin.web.Database;
import jamsex.framework.RequestInfo;
import jamsex.templates.Templates;

import java.io.IOException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import com.google.appengine.api.datastore.Entity;

public class AdminQrCode extends AdminPageHandler {

	public static void handle(RequestInfo info) throws IOException {

		if (!checkauth(info))
			return;
		
		if (info.isGet())
			handleGet(info);
		
		if (info.isPost())
			handlePost(info);
		
	}

	private static void handlePost(RequestInfo info) throws IOException {
		String action = info.getReq().getParameter("action");
		long qr_id = Long.parseLong(info.get(-1));		
		if ("unbind".equals(action)) {
			Entity qrEntity = Database.getQrCode(qr_id);
			if (qrEntity.getProperty("type").equals("session")) {
				qrEntity.removeProperty("session_id");
			}
			qrEntity.setProperty("type", "none");
			Database.save(qrEntity);
		}
		info.getResp().sendRedirect(info.getReq().getRequestURI());
	}

	private static void handleGet(RequestInfo info) throws IOException {
		
		long qr_id = Long.parseLong(info.get(-1));
		Entity qrEntity = Database.getQrCode(qr_id);
		
		String code_type = (String) qrEntity.getProperty("type");
		String landing_url = "http://jamsedev.appspot.com/" + Long.toString(qr_id, 36);
		String qr_url = "https://chart.googleapis.com/chart?chs=500x500&cht=qr&chl=" + landing_url;
		VelocityContext ctx = new VelocityContext();
		ctx.put("code_type", code_type);
		ctx.put("landing_url", landing_url);
		ctx.put("qr_url", qr_url);
		
		if (code_type.equals("session")) {
			Long session_id = (Long) qrEntity.getProperty("session_id");
			Entity sessionEntity = Database.getSession(session_id);
			ctx.put("session_name", sessionEntity.getProperty("name"));
			ctx.put("session_desc", sessionEntity.getProperty("desc"));
		}
		
		Template template = Templates.getTemplate("jamsex/templates/admin_qr_code.html");		
		template.merge(ctx, info.getResp().getWriter());		
	}

}
