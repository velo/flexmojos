package org.sonatype.flexmojos.components.publisher;

import java.io.File;

public interface FlexSDKPublisher
{

    String ADOBE_GROUP_ID = "com.adobe.flex";

    String COMPILER_GROUP_ID = ADOBE_GROUP_ID + ".compiler";

    String FRAMEWORK_GROUP_ID = ADOBE_GROUP_ID + ".framework";

    String ROLE = FlexSDKPublisher.class.getName();

    void publish( File sdkFolder, String version, int defaultPlayer, File overwriteLibFolder )
        throws PublishingException;

}
