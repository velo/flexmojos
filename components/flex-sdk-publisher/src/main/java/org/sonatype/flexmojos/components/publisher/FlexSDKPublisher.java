package org.sonatype.flexmojos.components.publisher;

import java.io.File;

public interface FlexSDKPublisher
{

    String ROLE = FlexSDKPublisher.class.getName();

    void publish( File sdkFolder, String version, int defaultPlayer, File overwriteLibFolder )
        throws PublishingException;

}
