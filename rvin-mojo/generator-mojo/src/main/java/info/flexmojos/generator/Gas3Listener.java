package info.flexmojos.generator;

import org.apache.maven.plugin.logging.Log;
import org.granite.generator.GenerationListener;
import org.granite.generator.as3.JavaFileGenerationUnit;

/**
 * Logging <code>GenerationListener</code>.
 * @author Juraj Burian
 *
 */
public class FlexMojosGenerationListener implements GenerationListener<JavaFileGenerationUnit>{

	private final Log log;
	
	/**
	 * @param log
	 */
	public FlexMojosGenerationListener(final Log log) {
		this.log = log;
	}

	public void error(String message) {
		log.error(message);
	}

	public void error(String message, Exception e) {
		log.error(message, e);
	}

	public void generating(JavaFileGenerationUnit unit) {
		log.info("  Generating: " + unit.getOutput());
	}

	public void info(String message) {
		log.info(message);
	}

	public void info(String message, Exception e) {
		log.info(message, e);
	}

	public void warn(String message) {
		log.warn(message);
	}

	public void warn(String message, Exception e) {
		log.warn(message, e);
	}

}
