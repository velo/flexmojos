package org.sonatype.flexmojos.plugin.lifecyclemapping;

import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.flexmojos.plugin.common.FlexExtension;

@Component( role = LifecycleMapping.class, hint = FlexExtension.SWF )
public class SwfLifecycleMapping
    extends AbstractActionScriptLifecycleMapping
    implements LifecycleMapping
{

    public String getCompiler()
    {
        return "org.sonatype.flexmojos:flexmojos-maven-plugin:compile-swf";
    }

}
