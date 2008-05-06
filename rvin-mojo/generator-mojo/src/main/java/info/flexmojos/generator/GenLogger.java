package info.flexmojos.generator;

import org.apache.maven.plugin.logging.Log;
import org.granite.generator.ant.Logger;

public class GenLogger extends Logger {

	private Log log;

	public GenLogger(Log log) {
		super(null);
		this.log = log;
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

	public void error(String message) {
		log.error(message);
	}

	public void error(String message, Exception e) {
		log.error(message, e);
	}

}
