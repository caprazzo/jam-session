package jamsex.admin.web.handlers;

import jamsex.admin.web.Database;
import jamsex.framework.RequestInfo;
import jamsex.templates.Templates;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;

public class ListQrCodes extends AdminPageHandler {

	public static void handle(RequestInfo info) throws IOException {
		
		if (!checkauth(info))
			return;
		
		if (info.isGet())
			handleGet(info);
		
	}

	private static void handleGet(RequestInfo info) throws IOException {		
		 //Iterable<Entity> iterator = Database.listQrCodes().asList(fetchOptions)
		//List<Entity> codes = Database.listQrCodes().asList(FetchOptions.Builder.withLimit(1000));
		Iterator<Entity> codes = Database.listQrCodes().asIterator();
		Template template = Templates.getTemplate("jamsex/templates/list_qr_codes.html");
		VelocityContext ctx = new VelocityContext();
		ctx.put("codes", codes);
		template.merge(ctx, info.getResp().getWriter());
		//String page = Templates.render("admin.listQrCodes", "codes", );
		//info.getResp().getWriter().write(page);
		
		
		/*
		String html = listQrCodesHtml(codes);
		
		
		InputStream template = info.getServletContext().getResourceAsStream("/jamsex/admin/list_qr_codes.html");
		new SkimpyTemplate(template)
			.add("codes", html)
			.write(info.getResp().getWriter());
		*/
	}
	
	private static Iterable<Map<String, Object>> toMaps(Iterable<Entity>  in) {
		final Iterator<Entity> iterator = in.iterator();
		return new Iterable<Map<String, Object>>() {
			
			@Override
			public Iterator<Map<String, Object>> iterator() {
				return new Iterator<Map<String, Object>>() {

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public Map<String, Object> next() {
						return iterator.next().getProperties();
					}

					@Override
					public void remove() {
						// TODO Auto-generated method stub						
					}
				};
			}
		};
	}
	

	private static String listQrCodesHtml(Iterable<Entity> codes) {
		StringBuilder b = new StringBuilder();
		for (Entity e : codes) {
			b.append("<li>")
				.append("<a href=\"/admin/qr_code/")
				.append(e.getKey().getId())
				.append("\">")
				.append(e.getKey().getId())
				.append("</a>")
				.append(" type: ").append(e.getProperty("type"))
			.append("</li>");						
		}
		return b.toString();
	}

}
