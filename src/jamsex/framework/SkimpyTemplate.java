package jamsex.framework;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Scanner;

/**
 * Poor man templating system
 */
public class SkimpyTemplate {

	private String template;

	private static String convertStreamToString(InputStream is) { 
	    return new Scanner(is).useDelimiter("\\A").next();
	}
	
	public SkimpyTemplate(InputStream template) throws IOException {
		this.template = convertStreamToString(template);
	}

	public SkimpyTemplate add(String key, String value) {
		template = template.replaceAll("\\{\\{ " + key + " \\}\\}", value);
		return this;
	}

	public void write(Writer out) throws IOException {
		out.write(template);
	}

}