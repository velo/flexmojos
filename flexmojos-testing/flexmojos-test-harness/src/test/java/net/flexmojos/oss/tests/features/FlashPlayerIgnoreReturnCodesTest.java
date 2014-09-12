package net.flexmojos.oss.tests.features;

import net.flexmojos.oss.test.FMVerifier;
import net.flexmojos.oss.tests.concept.AbstractConceptTest;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by christoferdutz on 12.09.14.
 */
public class FlashPlayerIgnoreReturnCodesTest
        extends AbstractConceptTest
{

    @Test
    public void ignoreByPomConfiguration()
            throws Exception
    {
        File testDir = getProject( "/features/flashplayer-ignore-return-codes" );
        FMVerifier v = test( testDir, "verify" );
        v.verifyTextInLog( "[LAUNCHER] runtime exited with ignored return code:" );
    }

    @Test
    public void ignoreByProperty()
            throws Exception
    {
        File testDir = getProject( "/concept/flexunit4-example" );
        FMVerifier v = test( testDir, "verify", "-Dflex.flashPlayer.returnCodesToIgnore=100,200,300,0" );
        v.verifyTextInLog( "[LAUNCHER] runtime exited with ignored return code:" );
    }

}
