package org.sonatype.flexmojos.plugin.lifecyclemapping;

import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.flexmojos.plugin.common.FlexExtension;

@Component( role = LifecycleMapping.class, hint = FlexExtension.SWC )
public class SwcLifecycleMapping
    extends AbstractActionScriptLifecycleMapping
    implements LifecycleMapping
{

    public String getCompiler()
    {
        return "org.sonatype.flexmojos:flexmojos-maven-plugin:compile-swc";
    }

    @Override
    protected String getPackage()
    {
        return "org.sonatype.flexmojos:flexmojos-maven-plugin:create-rsl,org.sonatype.flexmojos:flexmojos-maven-plugin:dita-asdoc";
    }

}
