package net.caprazzi.tapauth;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

/**
 * Poor man templating system
 * @author mcaprari
 */
public class SkimpyTemplate {

	private String template;

	public SkimpyTemplate(InputStream inputStream) throws IOException {
		template = Misc.readToString(inputStream);
	}

	public SkimpyTemplate add(String key, String value) {
		template = template.replaceAll("\\{\\{ " + key + " \\}\\}", value);
		return this;
	}
	
	public void write(Writer out) throws IOException {
		out.write(template);
	}
	
}
