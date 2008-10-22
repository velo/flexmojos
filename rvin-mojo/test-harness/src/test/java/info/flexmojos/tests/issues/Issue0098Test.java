package info.flexmojos.tests.issues;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.junit.Test;

public class Issue0098Test
    extends AbstractIssueTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void issue98()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0098" );

        Verifier verifier = getVerifier( testDir );
        verifier.getCliOptions().add( "-DinjectedNumber=3" );
        verifier.executeGoal( "install" );
        verifier.verifyErrorFreeLog();
    }

}
