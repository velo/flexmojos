package info.flexmojos.tests;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.embedder.Configuration;
import org.apache.maven.embedder.ConfigurationValidationResult;
import org.apache.maven.embedder.ContainerCustomizer;
import org.apache.maven.embedder.DefaultConfiguration;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderException;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.codehaus.plexus.PlexusContainer;
import org.junit.BeforeClass;

public class AbstractFlexMojosTests
{

    private static final ClassLoader CLASS_LOADER;
    static
    {
        CLASS_LOADER = new URLClassLoader( new URL[0], ClassLoader.getSystemClassLoader() );
        // CLASS_LOADER = ClassLoader.getSystemClassLoader();
    }

    protected static File rootFolder;

    @BeforeClass
    public static void initFolders()
        throws MavenEmbedderException
    {
        URL rootUrl = AbstractFlexMojosTests.class.getResource( "/" );
        rootFolder = new File( rootUrl.getFile() );
    }

    protected static void test( File projectDirectory, String goal, String... args )
        throws Exception
    {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try
        {
            Configuration configuration = new DefaultConfiguration();
            configuration.setUserSettingsFile( MavenEmbedder.DEFAULT_USER_SETTINGS_FILE );
            configuration.setConfigurationCustomizer( new ContainerCustomizer()
            {
                public void customize( PlexusContainer arg0 )
                {
                }
            } );
            // configuration.setGlobalSettingsFile( MavenEmbedder.DEFAULT_GLOBAL_SETTINGS_FILE );
            configuration.setClassLoader( CLASS_LOADER );
            // ClassLoader classLoader = MavenEmbedder.class.getClassLoader();
            // configuration.setClassLoader( classLoader );

            ConfigurationValidationResult validationResult = MavenEmbedder.validateConfiguration( configuration );

            if ( validationResult.isValid() )
            {
                List<String> goals = Arrays.asList( new String[] { "clean", goal } );

                MavenExecutionRequest request = new DefaultMavenExecutionRequest();
                request.setPom( new File( projectDirectory, "pom.xml" ) );
                request.setBaseDirectory( projectDirectory );
                request.setGoals( goals );
                request.setUpdateSnapshots( false );
                request.setUseReactor( false );

                MavenEmbedder embedder = new MavenEmbedder( configuration );
                MavenExecutionResult result = embedder.execute( request );
                if ( result.hasExceptions() )
                {
                    Exception exception = (Exception) result.getExceptions().get( 0 );
                    throw (Exception) exception.getCause();
                }
            }
            else
            {
                Assert.fail( "Invalid configuration!" + configuration.toString() );
            }

        }
        finally
        {
            Thread.currentThread().setContextClassLoader( orig );
        }
    }

    protected File getProject( String projectName )
    {
        File projectFolder = new File( rootFolder, projectName );
        Assert.assertTrue( "Project " + projectName + " folder not found.\n" + projectFolder.getAbsolutePath(),
                           projectFolder.isDirectory() );
        return projectFolder;
    }

}
