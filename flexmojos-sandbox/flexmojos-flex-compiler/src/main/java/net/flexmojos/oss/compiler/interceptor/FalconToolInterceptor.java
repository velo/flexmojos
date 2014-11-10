package net.flexmojos.oss.compiler.interceptor;

import net.flexmojos.oss.compiler.FlexCompiler;
import org.apache.flex.tools.FlexTool;
import org.apache.flex.tools.FlexToolGroup;
import org.codehaus.plexus.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by christoferdutz on 10.11.14.
 */
@Component( role = FlexToolInterceptor.class )
public class FalconToolInterceptor implements FlexToolInterceptor {

    @Override
    public String[] intercept(FlexToolGroup flexToolGroup, FlexTool flexTool, String[] args) {
        // All Falcon based compilers don't like empty arguments of the type: "-compilerOption=",
        // so we have to strip these out first.
        if("Falcon".equalsIgnoreCase(flexToolGroup.getName()) ||
                "FlexJS".equalsIgnoreCase(flexToolGroup.getName()) ||
                "VF2JS".equalsIgnoreCase(flexToolGroup.getName())) {
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
