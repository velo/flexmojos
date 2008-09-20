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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.it.Verifier;
import org.junit.BeforeClass;

public class AbstractFlexMojosTests
{

    protected static File projectsSource;

    protected static File projectsWorkdir;

    private static Properties props;

    @BeforeClass
    public static void initFolders()
        throws IOException
    {
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

        projectsSource = new File( getProperty( "projects-source" ) );
        projectsWorkdir = new File( getProperty( "projects-target" ) );
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
        // verifier.getCliOptions().add( "-s" + rootFolder.getAbsolutePath() + "/settings.xml" );
        // verifier.getCliOptions().add( "-o" );
        verifier.getCliOptions().addAll( Arrays.asList( args ) );
        verifier.getCliOptions().add( "-Dflex-mojos.version=" + getProperty( "version" ) );
        verifier.getVerifierProperties().put( "use.mavenRepoLocal", "true" );
        verifier.setLocalRepo( getProperty( "fake-repo" ) );
        verifier.executeGoal( goal );
        verifier.verifyErrorFreeLog();
    }

    protected File getProject( String projectName )
        throws IOException
    {
        File projectFolder = new File( projectsSource, projectName );
        Assert.assertTrue( "Project " + projectName + " folder not found.\n" + projectFolder.getAbsolutePath(),
                           projectFolder.isDirectory() );

        File destDir = new File( projectsWorkdir, projectName );
        FileUtils.copyDirectory( projectFolder, destDir );

        // projects filtering
        Collection<File> poms =
            FileUtils.listFiles( destDir, new WildcardFileFilter( "pom.xml" ), DirectoryFileFilter.INSTANCE );
        for ( File pom : poms )
        {
            String pomContent = FileUtils.readFileToString( pom );
            pomContent = pomContent.replace( "%{flex-mojos.version}", getProperty( "version" ) );
            FileUtils.writeStringToFile( pom, pomContent );
        }

        return destDir;
    }

}
