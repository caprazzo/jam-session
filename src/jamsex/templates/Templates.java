package jamsex.templates;

import java.io.File;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;


public class Templates {

	static {
		Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath"); 
		Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		VelocityContext context = new VelocityContext();
		context.put( "name", new String("Velocity") );
	}
	
	public static Template getTemplate(String name) {
		return Velocity.getTemplate(name);
	}
}
