/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
