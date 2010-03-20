package org.sonatype.flexmojos.plugin.truster;

import java.io.File;

public interface FlashPlayerTruster
{

    void updateSecuritySandbox( File trustedFile )
        throws TrustException;

    File getTrustDir();

}
