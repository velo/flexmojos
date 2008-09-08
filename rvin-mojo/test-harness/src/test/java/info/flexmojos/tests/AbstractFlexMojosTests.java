package info.flexmojos.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.maven.it.Verifier;
import org.junit.BeforeClass;

public class AbstractFlexMojosTests
{

    protected static File rootFolder;

    private static Properties props;

    @BeforeClass
    public static void initFolders()
        throws IOException
    {
        URL rootUrl = AbstractFlexMojosTests.class.getResource( "/" );
        rootFolder = new File( rootUrl.getFile() );

        props = new Properties();
        ClassLoader cl = AbstractFlexMojosTests.class.getClassLoader();
        InputStream is = cl.getResourceAsStream( "baseTest.properties" );
        if ( is != null )
        {
            try
            {
                props.load( is );
            }
            finally
            {
                is.close();
            }
        }
    }

    private static synchronized String getProperty( String key )
        throws IOException
    {
        return props.getProperty( key );
    }

    protected static void test( File projectDirectory, String goal, String... args )
        throws Exception
    {
        System.setProperty( "maven.home", getProperty( "fake-maven" ) );

        Verifier verifier = new Verifier( projectDirectory.getAbsolutePath() );
        verifier.resetStreams();
        // verifier.getCliOptions().add( "-s" + rootFolder.getAbsolutePath() + "/settings.xml" );
        verifier.getCliOptions().add( "-o" );
        verifier.getCliOptions().addAll( Arrays.asList( args ) );
        verifier.getVerifierProperties().put( "use.mavenRepoLocal", "true" );
        verifier.setLocalRepo( getProperty( "fake-repo" ) );
        verifier.executeGoal( goal );
        verifier.verifyErrorFreeLog();
    }

    protected File getProject( String projectName )
    {
        File projectFolder = new File( rootFolder, projectName );
        Assert.assertTrue( "Project " + projectName + " folder not found.\n" + projectFolder.getAbsolutePath(),
                           projectFolder.isDirectory() );
        return projectFolder;
    }

}
