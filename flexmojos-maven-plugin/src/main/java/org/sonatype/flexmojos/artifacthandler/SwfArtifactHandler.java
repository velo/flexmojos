package org.sonatype.flexmojos.artifacthandler;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.flexmojos.common.FlexExtension;

@Component( role = ArtifactHandler.class, hint = FlexExtension.SWF )
public class SwfArtifactHandler
    extends AbstractActionScriptArtifactHandler
    implements ArtifactHandler
{


    @Override
    public String getType()
    {
        return FlexExtension.SWF;
    }
}
