package info.rvin.mojo.flexmojo.compiler;

import org.apache.maven.plugin.logging.Log;

import flex2.tools.oem.Logger;
import flex2.tools.oem.Message;

public class CompileLogger implements Logger {

	private Log log;

	public CompileLogger(Log log) {
		this.log = log;
	}

	public void log(Message msg, int errorCode, String source) {
		if (Message.ERROR.equals(msg.getLevel())) {
			log.error(getMessage(msg, source));
		} else if (Message.INFO.equals(msg.getLevel())) {
			log.info(getMessage(msg, source));
		} else if (Message.WARNING.equals(msg.getLevel())) {
			log.warn(getMessage(msg, source));
		}
	}

	private String getMessage(Message msg, String source) {
		if (source != null && !"".equals(source)) {
			return msg.toString() + "\n" + source;
		}
		return msg.toString();
	}

}
