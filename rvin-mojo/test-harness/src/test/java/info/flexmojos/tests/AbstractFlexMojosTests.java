/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
