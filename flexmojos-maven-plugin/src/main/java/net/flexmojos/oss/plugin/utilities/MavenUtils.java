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
package net.flexmojos.oss.plugin.utilities;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import scala.actors.threadpool.Arrays;

/**
 * Utility class to help get information from Maven objects like files, source paths, resolve dependencies, etc.
 * 
 * @author velo.br
 */
public class MavenUtils
{

    private static final String NET_BSD = "netbsd";

    private static final String FREE_BSD = "freebsd";

    private static final String WINDOWS_OS = "windows";

    private static final String MAC_OS = "mac os x";

    private static final String MAC_OS_DARWIN = "darwin";

    private static final String LINUX_OS = "linux";

    private static final String SOLARIS_OS = "sunos";

    private static final String VISTA = "vista";

    private static final Properties flexmojosProperties;

    static
    {
        flexmojosProperties = new Properties();
        try
        {
            flexmojosProperties.load( MavenUtils.class.getResourceAsStream( "/flexmojos.properties" ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Unable to load flexmojos.properties", e );
        }
    }

    private MavenUtils()
    {
    }

    /**
     * Resolve a resource file in a maven project resources folders
     * 
     * @param project maven project
     * @param fileName sugested name on pom
     * @return source file or null if source not found
     * @throws MojoFailureException
     */
    public static File resolveResourceFile( MavenProject project, String fileName )
        throws MojoFailureException
    {

        File file = new File( fileName );

        if ( file.exists() )
        {
            return file;
        }

        if ( file.isAbsolute() )
        {
            throw new MojoFailureException( "File " + fileName + " not found" );
        }

        List<Resource> resources = project.getBuild().getResources();

        for ( Resource resourceFolder : resources )
        {
            File resource = new File( resourceFolder.getDirectory(), fileName );
            if ( resource.exists() )
            {
                return resource;
            }
        }

        throw new MojoFailureException( "File " + fileName + " not found" );
    }

    /**
     * Returns the file reference to the fonts file. Depending on the os, the correct fonts.ser file is used. The fonts
     * file is copied to the build directory.
     * 
     * @param build Build for which to get the fonts file
     * @return file reference to fonts file
     * @throws MojoExecutionException thrown if the config file could not be copied to the build directory
     */
    public static File getFontsFile( Build build )
        throws MojoExecutionException
    {
        URL url;
        if ( MavenUtils.isMac() )
        {
            url = MavenUtils.class.getResource( "/fonts/macFonts.ser" );
        }
        else
        {
            // TODO And linux?!
            // if(os.contains("windows")) {
            url = MavenUtils.class.getResource( "/fonts/winFonts.ser" );
        }
        File fontsSer = new File( build.getOutputDirectory(), "fonts.ser" );
        try
        {
            if (!fontsSer.exists())
                FileUtils.copyURLToFile( url, fontsSer );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error copying fonts file.", e );
        }
        return fontsSer;
    }

    /**
     * Returns the file reference to a localize resourceBundlePath. Replaces the {locale} variable in the given
     * resourceBundlePath with given locale.
     * 
     * @param resourceBundlePath Path to resource bundle.
     * @param locale Locale
     * @return File reference to the resourceBundlePath for given locale
     */
    public static File getLocaleResourcePath( String resourceBundlePath, String locale )
    {
        String path = resourceBundlePath.replace( "{locale}", locale );
        File localePath = new File( path );
        return localePath.exists() ? localePath : null;
    }

    public static String osString()
    {
        return System.getProperty( "os.name" ).toLowerCase();
    }

    /**
     * Return a boolean to show if we are running on Windows.
     * 
     * @return true if we are running on Windows.
     */
    public static boolean isWindows()
    {
        return osString().startsWith( WINDOWS_OS );
    }

    /**
     * Return a boolean to show if we are running on Linux.
     * 
     * @return true if we are running on Linux.
     */
    public static boolean isLinux()
    {
        return osString().startsWith( LINUX_OS ) ||
        // I know, but people said that workds...
            osString().startsWith( NET_BSD ) ||
            osString().startsWith( FREE_BSD );
    }

    /**
     * Return a boolean to show if we are running on Solaris.
     * 
     * @return true if we are running on Solaris.
     */
    public static boolean isSolaris()
    {
        return osString().startsWith( SOLARIS_OS );
    }

    /**
     * Return a boolean to show if we are running on a unix-based OS.
     * 
     * @return true if we are running on a unix-based OS.
     */
    public static boolean isUnixBased()
    {
        return isLinux() || isSolaris();
    }

    /**
     * Return a boolean to show if we are running on Mac OS X.
     * 
     * @return true if we are running on Mac OS X.
     */
    public static boolean isMac()
    {
        return osString().startsWith( MAC_OS ) || osString().startsWith( MAC_OS_DARWIN );
    }

    /**
     * Return a boolean to show if we are running on Windows Vista.
     * 
     * @return true if we are running on Windows Vista.
     */
    public static boolean isWindowsVista()
    {
        return osString().startsWith( WINDOWS_OS ) && osString().contains( VISTA );
    }

    public static Artifact searchFor( Collection<Artifact> artifacts, String groupId, String artifactId,
                                      String version, String type, String classifier )
    {
        for ( Artifact artifact : artifacts )
        {
            if ( equals( artifact.getGroupId(), groupId ) && equals( artifact.getArtifactId(), artifactId )
                && equals( artifact.getVersion(), version ) && equals( artifact.getType(), type )
                && equals( artifact.getClassifier(), classifier ) )
            {
                return artifact;
            }
        }

        return null;
    }

    private static boolean equals( String str1, String str2 )
    {
        // If is null is not relevant
        if ( str1 == null || str2 == null )
        {
            return true;
        }

        return str1.equals( str2 );
    }

    public static String getFlexMojosVersion()
    {
        return flexmojosProperties.getProperty( "version" );
    }

    public static String replaceArtifactCoordinatesTokens( String sample, Artifact artifact )
    {
        sample = sample.replace( "{groupId}", artifact.getGroupId() );
        sample = sample.replace( "{artifactId}", artifact.getArtifactId() );
        sample = sample.replace( "{version}", artifact.getBaseVersion() );

        if(sample.contains("{hash}"))
        {
            String hash = "CANT_CALCULATE_HASH";
            try
            {
                byte[] artifactBytes = org.apache.commons.io.FileUtils.readFileToByteArray( artifact.getFile() );
                hash = DigestUtils.md5Hex(artifactBytes);
            }
            catch (IOException ignored)
            {
            }
            sample = sample.replace( "{hash}", hash );
        }

        if ( artifact.getClassifier() != null )
        {
            sample = sample.replace( "{classifier}", artifact.getClassifier() );
        }
        sample = sample.replace( "{hard-version}", artifact.getVersion() );

        return sample;
    }

    public static String interpolateRslUrl( String baseUrl, Artifact artifact, String extension, String contextRoot )
    {
        if ( baseUrl == null )
        {
            return null;
        }

        if ( contextRoot == null || "".equals( contextRoot ) )
        {
            baseUrl = baseUrl.replace( "{contextRoot}/", "" );
        }
        else
        {
            baseUrl = baseUrl.replace( "{contextRoot}", contextRoot );
        }

        baseUrl = replaceArtifactCoordinatesTokens( baseUrl, artifact );

        if ( extension != null )
        {
            baseUrl = baseUrl.replace( "{extension}", extension );
        }

        return baseUrl;
    }

    public static String getRuntimeLocaleOutputPath( String sample, Artifact artifact, String locale, String extension )
    {
        String path = replaceArtifactCoordinatesTokens( sample, artifact );
        path = path.replace( "{locale}", locale );
        path = path.replace( "{extension}", extension );

        return path;
    }

    public static String getRuntimeLocaleOutputName( String sample, Artifact artifact, String locale )
    {
        String path = replaceArtifactCoordinatesTokens( sample, artifact );
        path = path.replace( "{locale}", locale );

        return path;
    }

    public static Set<File> getFilesSet( Collection<Artifact>... dependenciesSet )
    {
        if ( dependenciesSet == null )
        {
            return null;
        }

        Set<File> files = new LinkedHashSet<File>();
        for ( Collection<Artifact> dependencies : dependenciesSet )
        {
            if ( dependencies == null )
            {
                continue;
            }

            for ( Artifact artifact : dependencies )
            {
                if ( !artifact.isResolved() )
                {
                    throw new IllegalArgumentException( "Unable to handle unresolved artifact: " + artifact );
                }

                if ( artifact.getFile() == null )
                {
                    throw new NullPointerException( "Artifact file not defined: " + artifact );
                }

                files.add( artifact.getFile() );
            }
        }
        return files;
    }

    @SuppressWarnings( "unchecked" )
    public static File[] getFiles( Artifact... dependencies )
    {
        if ( dependencies == null )
        {
            return null;
        }
        final Set<File> filesSet = getFilesSet(Arrays.asList(dependencies));
        return filesSet.toArray(new File[filesSet.size()]);
    }

    public static File[] getFiles( Collection<Artifact>... dependencies )
    {
        if ( dependencies == null )
        {
            return null;
        }

        final Set<File> filesSet = getFilesSet(dependencies);
        return filesSet.toArray(new File[filesSet.size()]);
    }

}
