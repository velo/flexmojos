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
            if("Falcon".equalsIgnoreCase(flexToolGroup.getName()) ||
                    "FlexJS".equalsIgnoreCase(flexToolGroup.getName())) {
                filteredArgs.add("-compiler.mxml.children-as-data");
                filteredArgs.add("-compiler.binding-value-change-event=org.apache.flex.events.ValueChangeEvent");
                filteredArgs.add("-compiler.binding-value-change-event-kind=org.apache.flex.events.ValueChangeEvent");
                filteredArgs.add("-compiler.binding-value-change-event-type=valueChange");
                filteredArgs.add("-compiler.binding-event-handler-class=org.apache.flex.events.EventDispatcher");
                filteredArgs.add("-compiler.binding-event-handler-event=org.apache.flex.events.Event");
                filteredArgs.add("-compiler.component-factory-class=org.apache.flex.core.ClassFactory");
                filteredArgs.add("-compiler.component-factory-interface=org.apache.flex.core.IFactory");
                filteredArgs.add("-compiler.show-unused-type-selector-warnings=true");
                filteredArgs.add("-compiler.states-class=org.apache.flex.states.State");
                filteredArgs.add("-compiler.states-event-override-class=org.apache.flex.states.SetEventHandler");
                filteredArgs.add("-compiler.states-instance-override-class=org.apache.flex.states.AddItems");
                filteredArgs.add("-compiler.states-property-override-class=org.apache.flex.states.SetProperty");
                filteredArgs.add("-compiler.warn-array-tostring-changes=false");
                filteredArgs.add("-compiler.warn-assignment-within-conditional=true");
                filteredArgs.add("-compiler.warn-bad-array-cast=true");
                filteredArgs.add("-compiler.warn-bad-bool-assignment=true");
                filteredArgs.add("-compiler.warn-bad-date-cast=true");
                filteredArgs.add("-compiler.warn-bad-es3-type-method=true");
                filteredArgs.add("-compiler.warn-bad-es3-type-prop=true");
                filteredArgs.add("-compiler.warn-bad-nan-comparison=true");
                filteredArgs.add("-compiler.warn-bad-null-assignment=true");
                filteredArgs.add("-compiler.warn-bad-null-comparison=true");
                filteredArgs.add("-compiler.warn-bad-undefined-comparison=true");
                filteredArgs.add("-compiler.warn-boolean-constructor-with-no-args=false");
                filteredArgs.add("-compiler.warn-changes-in-resolve=false");
                filteredArgs.add("-compiler.warn-class-is-sealed=true");
                filteredArgs.add("-compiler.warn-const-not-initialized=true");
                filteredArgs.add("-compiler.warn-constructor-returns-value=false");
                filteredArgs.add("-compiler.warn-deprecated-event-handler-error=false");
                filteredArgs.add("-compiler.warn-deprecated-function-error=true");
                filteredArgs.add("-compiler.warn-deprecated-property-error=true");
                filteredArgs.add("-compiler.warn-duplicate-argument-names=true");
                filteredArgs.add("-compiler.warn-duplicate-variable-def=true");
                filteredArgs.add("-compiler.warn-for-var-in-changes=false");
                filteredArgs.add("-compiler.warn-import-hides-class=true");
                filteredArgs.add("-compiler.warn-instance-of-changes=true");
                filteredArgs.add("-compiler.warn-internal-error=true");
                filteredArgs.add("-compiler.warn-level-not-supported=true");
                filteredArgs.add("-compiler.warn-missing-namespace-decl=true");
                filteredArgs.add("-compiler.warn-negative-uint-literal=true");
                filteredArgs.add("-compiler.warn-no-constructor=false");
                filteredArgs.add("-compiler.warn-no-explicit-super-call-in-constructor=false");
                filteredArgs.add("-compiler.warn-no-type-decl=true");
                filteredArgs.add("-compiler.warn-number-from-string-changes=false");
                filteredArgs.add("-compiler.warn-scoping-change-in-this=false");
                filteredArgs.add("-compiler.warn-slow-text-field-addition=true");
                filteredArgs.add("-compiler.warn-unlikely-function-value=true");
                filteredArgs.add("-compiler.warn-xml-class-has-changed=false");
                filteredArgs.add("-remove-unused-rsls=true");
                filteredArgs.add("-static-link-runtime-shared-libraries=true");
            }
            args = filteredArgs.toArray(new String[filteredArgs.size()]);
        }

        return args;
    }

}
