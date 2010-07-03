package org.sonatype.flexmojos.tests.issues;

import java.net.ServerSocket;

import org.testng.annotations.Test;

public class Flexmojos321Test
    extends AbstractIssueTest
{

    @Test
    public void multiple()
        throws Throwable
    {
        ServerSocket ss = new ServerSocket( 13539 );
        try
        {
            testIssue( "flexmojos-321/multiple" );
        }
        finally
        {
            ss.close();
        }
    }
}
