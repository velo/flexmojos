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
 * In contrast to the old Legacy compiler, the new Falcon compiler
 * doesn't accept empty commandline options. In order to avoid errors
 * we simply filter out empty options.
 *
 * Created by christoferdutz on 10.11.14.
 */
@Component( role = FlexToolInterceptor.class, hint = "flex-tool-interceptor")
public class FalconToolInterceptor implements FlexToolInterceptor {

    @Override
    public String[] interceptArgs(FlexToolGroup flexToolGroup, FlexTool flexTool, String[] args) {
        // All Falcon based compilers don't like empty arguments of the type: "-compilerOption=",
        // so we have to strip these out first.
        if("Falcon".equalsIgnoreCase(flexToolGroup.getName()) || "FlexJS".equalsIgnoreCase(flexToolGroup.getName()) ||
                "VF2JS".equalsIgnoreCase(flexToolGroup.getName()) ) {
            List<String> filteredArgs = new ArrayList<String>();
            for(String arg : args) {
                if(!arg.endsWith("=")) {
                    filteredArgs.add(arg);
                }
            }
            args = filteredArgs.toArray(new String[filteredArgs.size()]);
        }

        return args;
    }

}
