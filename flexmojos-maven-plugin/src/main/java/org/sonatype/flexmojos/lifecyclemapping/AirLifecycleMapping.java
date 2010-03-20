package org.sonatype.flexmojos.lifecyclemapping;

import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.flexmojos.common.FlexExtension;

@Component( role = LifecycleMapping.class, hint = FlexExtension.AIR )
public class AirLifecycleMapping
    extends AbstractActionScriptLifecycleMapping
    implements LifecycleMapping
{

    public String getCompiler()
    {
        return "org.sonatype.flexmojos:flexmojos-maven-plugin:compile-swf";
    }

    @Override
    protected String getPackage()
    {
        return "org.sonatype.flexmojos:flexmojos-maven-plugin:sign-air";
    }

}
