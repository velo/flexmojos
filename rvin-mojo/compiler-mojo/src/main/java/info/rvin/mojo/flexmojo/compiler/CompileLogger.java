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
			log.info(msg.toString());
		} else if (Message.WARNING.equals(msg.getLevel())) {
			log.warn(getMessage(msg, source));
		}
	}

	private String getMessage(Message msg, String source) {
		StringBuilder sb = new StringBuilder();
		sb.append(msg.getPath());
		sb.append(':');
		sb.append('[');
		sb.append(msg.getLine());
		sb.append(',');
		sb.append(msg.getColumn());
		sb.append(']');
		sb.append(' ');
		sb.append(msg.toString());
		return sb.toString();
	}

}
