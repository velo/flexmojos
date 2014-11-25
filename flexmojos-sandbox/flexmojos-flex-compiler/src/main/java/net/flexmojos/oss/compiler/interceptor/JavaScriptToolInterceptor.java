/**
 * Flexmojos is a set of maven goals to allow maven users to compile,
 * optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
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

package net.flexmojos.oss.compiler.interceptor;

import org.apache.flex.tools.FlexTool;
import org.apache.flex.tools.FlexToolGroup;
import org.codehaus.plexus.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * When performing a build with JavaScript output, the output parameter should
 * refer to a directory and not a fie. So if the build is done using a JavaScript
 * output tool, replace the "output" commandline switch.
 *
 * Created by christoferdutz on 11.11.14.
 */
@Component( role = FlexToolInterceptor.class, hint = "javascript-tool-interceptor")
public class JavaScriptToolInterceptor implements FlexToolInterceptor {

    @Override
    public String[] intercept(FlexToolGroup flexToolGroup, FlexTool flexTool, String[] args) {
        if("FlexJS".equalsIgnoreCase(flexToolGroup.getName()) ||
                "VF2JS".equalsIgnoreCase(flexToolGroup.getName())) {
            List<String> filteredArgs = new ArrayList<String>();
            for(String arg : args) {
                if(arg.startsWith("-output=")) {
                    // Cut of the file ending.
                    filteredArgs.add(arg.substring(0, arg.lastIndexOf(".")));
                } else {
                    filteredArgs.add(arg);
                }
            }
            args = filteredArgs.toArray(new String[filteredArgs.size()]);
        }
        return args;
    }

}
