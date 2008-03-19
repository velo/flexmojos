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
	    log.error(msg.toString());
	} else 
	if (Message.INFO.equals(msg.getLevel())) {
	    log.info(msg.toString());
	} else 
	if (Message.WARNING.equals(msg.getLevel())) {
	    log.warn(msg.toString());
	}
    }

}
