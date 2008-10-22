package info.rvin.mojo.flexmojo.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import info.flexmojos.tests.AbstractFlexMojosTests;

import java.io.File;
import java.io.FileReader;

import org.apache.maven.it.VerificationException;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Test;

public class IT0013IssuesTest
    extends AbstractFlexMojosTests
{

    public void testIssue( String issueNumber )
        throws Exception
    {
        File testDir = getProject( "/issues/" + issueNumber );
        test( testDir, "install" );
    }

    @Test
    public void issue8()
        throws Exception
    {
        testIssue( "issue-0008-1" );
        testIssue( "issue-0008-2" );
    }

    @Test
    public void issue11()
        throws Exception
    {
        testIssue( "issue-0011" );
    }

    @Test
    public void issue13()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0013" );
        test( testDir, "install" );

        File reportDir = new File( testDir, "target/surefire-reports" );
        assertEquals( 2, reportDir.listFiles().length );
    }

    @Test( expected = VerificationException.class )
    public void issue14()
        throws Exception
    {
        testIssue( "issue-0014" );
    }

    @Test
    public void issue15()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0015" );
        try
        {
            test( testDir, "install" );
            fail( "ing error unit, must fail!" );
        }
        catch ( VerificationException e )
        {
            // expected exception
        }

        File reportDir = new File( testDir, "target/surefire-reports" );
        assertEquals( 2, reportDir.listFiles().length );
    }

    @Test
    public void issue17()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0017" );
        test( testDir, "site" );

        File asdoc = new File( testDir, "target/site/asdoc" );
        assertTrue( "asdoc directory must exist", asdoc.isDirectory() );
    }

    @Test( expected = VerificationException.class )
    public void issue27()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0027" );
        test( testDir, "asdoc:asdoc" );
    }

    @Test
    public void issue29()
        throws Exception
    {
        testIssue( "issue-0029" );
    }

    @Test
    public void issue32()
        throws Exception
    {
        testIssue( "issue-0032" );
    }

    @Test
    public void issue39()
        throws Exception
    {
        testIssue( "issue-0039" );
    }

    // A wierd but on this tests
    // @Test public void issue43() throws Exception {
    // File testDir = ResourceExtractor.simpleExtractResources(getClass(),
    // "/issues/issue-0014");
    // List<String> args = new ArrayList<String>();
    // args.add("-Dmaven.test.failure.ignore=true");
    // test(testDir, "info.rvin.itest.issues", "issue-0014",
    // "1.0-SNAPSHOT", "swf", "install", args);
    // }

    @Test( expected = VerificationException.class )
    public void issue44()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0044" );
        test( testDir, "asdoc:asdoc" );
    }

    @Test
    public void issue53()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0014" );
        test( testDir, "install", "-Dmaven.test.skip=true" );

        test( testDir, "install", "-DskipTests=true" );
    }

    @Test
    public void issue61()
        throws Exception
    {
        testIssue( "issue-0061" );
    }

    @Test
    public void issue66()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0066" );
        testIssue( "issue-0066" );

        // Issue 62 test
        File another = new File( testDir, "flex/src/main/flex/info/flexmojos/generator/AnotherPojo.as" );
        assertFalse( another.exists() );

        // Issue 65 test
        File pojo = new File( testDir, "flex/src/main/flex/info/flexmojos/generator/SimplePojo.as" );
        assertTrue( pojo.exists() );
        File base =
            new File( testDir, "flex/target/generated-sources/flex-mojos/info/flexmojos/generator/SimplePojoBase.as" );
        assertTrue( base.exists() );
    }

    @Test
    public void issue67()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0067" );
        test( testDir, "asdoc:asdoc" );
    }

    @Test
    public void issue68()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0068" );
        test( testDir, "asdoc:asdoc" );
    }

    @Test
    public void issue69()
        throws Exception
    {
        final String[] trusts =
            new String[] { "AppData/Roaming/Macromedia/Flash Player/#Security/FlashPlayerTrust",
                "Application Data/Macromedia/Flash Player/#Security/FlashPlayerTrust",
                ".macromedia/Flash_Player/#Security/FlashPlayerTrust",
                "Library/Preferences/Macromedia/Flash Player/#Security/FlashPlayerTrust" };

        File userHome = new File( System.getProperty( "user.home" ) );

        File mavenCfg = null;
        for ( String folder : trusts )
        {
            File fpTrustFolder = new File( userHome, folder );
            if ( fpTrustFolder.exists() && fpTrustFolder.isDirectory() )
            {
                mavenCfg = new File( fpTrustFolder, "maven.cfg" );
                if ( mavenCfg.exists() )
                {
                    mavenCfg.delete();
                }
                break;
            }
        }

        testIssue( "issue-0069" );

        File testDir = getProject( "/issues/issue-0069" );
        File swf = new File( testDir, "target/test-classes/TestRunner.swf" );

        assertTrue( "Flex-mojos should generate maven.cfg: " + mavenCfg.getAbsolutePath(), mavenCfg.exists() );

        String cfg = IOUtil.toString( new FileReader( mavenCfg ) );

        assertTrue( "Flex-mojos should write trust localtion", cfg.contains( swf.getAbsolutePath() ) );
    }

    @Test
    public void issue70()
        throws Exception
    {
        testIssue( "issue-0070" );
    }

    @Test
    public void issue103()
        throws Exception
    {
        testIssue( "issue-0103/project" );
        // TODO check SWC content
    }

}
