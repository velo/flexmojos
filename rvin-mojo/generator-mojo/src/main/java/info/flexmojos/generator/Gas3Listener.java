package info.flexmojos.generator;

import org.apache.maven.plugin.logging.Log;
import org.granite.generator.GenerationListener;
import org.granite.generator.as3.JavaFileGenerationUnit;

/**
 * Logging <code>GenerationListener</code>.
 * @author Juraj Burian
 *
 */
public class Gas3Listener implements GenerationListener<JavaFileGenerationUnit>{

	private final Log log;

	/**
	 * @param log
	 */
	public Gas3Listener(final Log log) {
		this.log = log;
	}

	public void error(String message) {
		error(message, null);
	}

	public void error(String message, Exception e) {
		log.error(message, e);
	}

	public void generating(JavaFileGenerationUnit unit) {
		info("  Generating: " + unit.getOutput());
	}

	public void info(String message) {
		info(message, null);
	}

	public void info(String message, Exception e) {
		log.info(message, e);
	}

	public void warn(String message) {
		warn(message, null);
	}

	public void warn(String message, Exception e) {
		log.warn(message, e);
	}

}
