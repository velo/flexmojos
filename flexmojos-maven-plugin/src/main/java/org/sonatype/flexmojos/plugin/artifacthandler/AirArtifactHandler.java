package org.sonatype.flexmojos.plugin.artifacthandler;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.flexmojos.plugin.common.FlexExtension;

@Component( role = ArtifactHandler.class, hint = FlexExtension.AIR )
public class AirArtifactHandler
    extends AbstractActionScriptArtifactHandler
    implements ArtifactHandler
{

    @Override
    public String getType()
    {
        return FlexExtension.AIR;
    }
}
