package org.sonatype.flexmojos.test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.Assert;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.util.FileUtils;
import org.apache.maven.it.util.IOUtil;
import org.apache.maven.it.util.StringUtils;
import org.apache.maven.it.util.cli.CommandLineException;
import org.apache.maven.it.util.cli.CommandLineUtils;
import org.apache.maven.it.util.cli.Commandline;
import org.apache.maven.it.util.cli.StreamConsumer;
import org.apache.maven.it.util.cli.WriterStreamConsumer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import ch.lambdaj.Lambda;
import ch.lambdaj.function.aggregate.Concat;

/**
 * @author Jason van Zyl
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author Marvin Froeder
 * @noinspection UseOfSystemOutOrSystemErr,RefusedBequest
 */
public class FMVerifier
{
    private static final String LOG_FILENAME = "log.txt";

    public static String localRepo;

    private final String basedir;

    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    private final ByteArrayOutputStream errStream = new ByteArrayOutputStream();

    private PrintStream originalOut;

    private PrintStream originalErr;

    private List<String> cliOptions = new ArrayList<String>();

    private Properties systemProperties = new Properties();

    private Properties verifierProperties = new Properties();

    private boolean autoclean = true;

    // TODO: needs to be configurable
    private static String localRepoLayout = "default";

    private boolean debug;

    private boolean forkJvm = true;

    private String logFileName = LOG_FILENAME;

    private String defaultMavenHome;

    // will launch mvn with --debug
    private boolean mavenDebug = false;

    public FMVerifier( String basedir, String settingsFile )
        throws VerificationException
    {
        this( basedir, settingsFile, false );
    }

    public FMVerifier( String basedir, String settingsFile, boolean debug )
        throws VerificationException
    {
        this( basedir, settingsFile, debug, true );
    }

    public FMVerifier( String basedir, String settingsFile, boolean debug, boolean forkJvm )
        throws VerificationException
    {
        this.basedir = basedir;

        this.debug = debug;

        this.forkJvm = forkJvm;

        if ( !debug )
        {
            originalOut = System.out;

            System.setOut( new PrintStream( outStream ) );

            originalErr = System.err;

            System.setErr( new PrintStream( errStream ) );
        }

        findLocalRepo( settingsFile );
        findDefaultMavenHome();
    }

    private void findDefaultMavenHome()
        throws VerificationException
    {
        defaultMavenHome = System.getProperty( "maven.home" );

        if ( defaultMavenHome == null )
        {
            try
            {
                Properties envVars = CommandLineUtils.getSystemEnvVars();
                defaultMavenHome = envVars.getProperty( "M2_HOME" );
            }
            catch ( IOException e )
            {
                throw new VerificationException( "Cannot read system environment variables.", e );
            }
        }
    }

    public FMVerifier( String basedir )
        throws VerificationException
    {
        this( basedir, null );
    }

    public FMVerifier( String basedir, boolean debug )
        throws VerificationException
    {
        this( basedir, null, debug );
    }

    public static void setLocalRepo( String localRepo )
    {
        FMVerifier.localRepo = localRepo;
    }

    public void resetStreams()
    {
        if ( !debug )
        {
            System.setOut( originalOut );

            System.setErr( originalErr );
        }
    }

    public void displayStreamBuffers()
    {
        String out = outStream.toString();

        if ( out != null && out.trim().length() > 0 )
        {
            System.out.println( "----- Standard Out -----" );

            System.out.println( out );
        }

        String err = errStream.toString();

        if ( err != null && err.trim().length() > 0 )
        {
            System.err.println( "----- Standard Error -----" );

            System.err.println( err );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void verify( boolean chokeOnErrorOutput )
        throws VerificationException
    {
        List<String> lines = loadFile( getBasedir(), "expected-results.txt", false );

        for ( Iterator<String> i = lines.iterator(); i.hasNext(); )
        {
            String line = i.next();

            verifyExpectedResult( line );
        }

        if ( chokeOnErrorOutput )
        {
            verifyErrorFreeLog();
        }
    }

    public void verifyErrorFreeLog()
        throws VerificationException
    {
        List<String> lines;
        lines = loadFile( getBasedir(), getLogFileName(), false );

        for ( Iterator<String> i = lines.iterator(); i.hasNext(); )
        {
            String line = i.next();

            // A hack to keep stupid velocity resource loader errors from triggering failure
            if ( line.indexOf( "[ERROR]" ) >= 0 && !isVelocityError( line ) )
            {
                throw new VerificationException( "Error in execution: " + line );
            }
        }
    }

    /**
     * Checks whether the specified line is just an error message from Velocity. Especially old versions of Doxia employ
     * a very noisy Velocity instance.
     * 
     * @param line The log line to check, must not be <code>null</code>.
     * @return <code>true</code> if the line appears to be a Velocity error, <code>false</code> otherwise.
     */
    private static boolean isVelocityError( String line )
    {
        if ( line.indexOf( "VM_global_library.vm" ) >= 0 )
        {
            return true;
        }
        if ( line.indexOf( "VM #" ) >= 0 && line.indexOf( "macro" ) >= 0 )
        {
            // [ERROR] VM #displayTree: error : too few arguments to macro. Wanted 2 got 0
            return true;
        }
        return false;
    }

    /**
     * Throws an exception if the text is not present in the log.
     * 
     * @param text
     * @throws VerificationException
     */
    public void verifyTextInLog( String text )
        throws VerificationException
    {
        List<String> lines;
        lines = loadFile( getBasedir(), getLogFileName(), false );

        boolean result = false;
        for ( Iterator<String> i = lines.iterator(); i.hasNext(); )
        {
            String line = i.next();
            if ( line.indexOf( text ) >= 0 )
            {
                result = true;
                break;
            }
        }
        if ( !result )
        {
            throw new VerificationException( "Text not found in log: " + text );
        }
    }

    public Properties loadProperties( String filename )
        throws VerificationException
    {
        Properties properties = new Properties();

        try
        {
            File propertiesFile = new File( getBasedir(), filename );
            if ( propertiesFile.exists() )
            {
                FileInputStream fis = new FileInputStream( propertiesFile );
                try
                {
                    properties.load( fis );
                }
                finally
                {
                    fis.close();
                }
            }
        }
        catch ( FileNotFoundException e )
        {
            throw new VerificationException( "Error reading properties file", e );
        }
        catch ( IOException e )
        {
            throw new VerificationException( "Error reading properties file", e );
        }

        return properties;
    }

    /**
     * Loads the (non-empty) lines of the specified text file.
     * 
     * @param filename The path to the text file to load, relative to the base directory, must not be <code>null</code>.
     * @param encoding The character encoding of the file, may be <code>null</code> or empty to use the platform default
     *            encoding.
     * @return The list of (non-empty) lines from the text file, can be empty but never <code>null</code>.
     * @throws IOException If the file could not be loaded.
     * @since 1.2
     */
    public List<String> loadLines( String filename, String encoding )
        throws IOException
    {
        List<String> lines = new ArrayList<String>();

        File file = new File( getBasedir(), filename );

        BufferedReader reader = null;
        try
        {
            if ( StringUtils.isNotEmpty( encoding ) )
            {
                reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), encoding ) );
            }
            else
            {
                reader = new BufferedReader( new FileReader( file ) );
            }

            String line;
            while ( ( line = reader.readLine() ) != null )
            {
                if ( line.length() > 0 )
                {
                    lines.add( line );
                }
            }
        }
        finally
        {
            IOUtil.close( reader );
        }

        return lines;
    }

    public List<String> loadFile( String basedir, String filename, boolean hasCommand )
        throws VerificationException
    {
        return loadFile( new File( basedir, filename ), hasCommand );
    }

    public List<String> loadFile( File file, boolean hasCommand )
        throws VerificationException
    {
        List<String> lines = new ArrayList<String>();

        if ( file.exists() )
        {
            try
            {
                BufferedReader reader = new BufferedReader( new FileReader( file ) );

                String line = reader.readLine();

                while ( line != null )
                {
                    line = line.trim();

                    if ( !line.startsWith( "#" ) && line.length() != 0 )
                    {
                        lines.addAll( replaceArtifacts( line, hasCommand ) );
                    }
                    line = reader.readLine();
                }

                reader.close();
            }
            catch ( FileNotFoundException e )
            {
                throw new VerificationException( e );
            }
            catch ( IOException e )
            {
                throw new VerificationException( e );
            }
        }

        return lines;
    }

    private List<String> replaceArtifacts( String line, boolean hasCommand )
    {
        String MARKER = "${artifact:";
        int index = line.indexOf( MARKER );
        if ( index >= 0 )
        {
            String newLine = line.substring( 0, index );
            index = line.indexOf( "}", index );
            if ( index < 0 )
            {
                throw new IllegalArgumentException( "line does not contain ending artifact marker: '" + line + "'" );
            }
            String artifact = line.substring( newLine.length() + MARKER.length(), index );

            newLine += getArtifactPath( artifact );
            newLine += line.substring( index + 1 );

            List<String> l = new ArrayList<String>();
            l.add( newLine );

            int endIndex = newLine.lastIndexOf( '/' );

            String command = null;
            String filespec;
            if ( hasCommand )
            {
                int startIndex = newLine.indexOf( ' ' );

                command = newLine.substring( 0, startIndex );

                filespec = newLine.substring( startIndex + 1, endIndex );
            }
            else
            {
                filespec = newLine;
            }

            File dir = new File( filespec );
            addMetadataToList( dir, hasCommand, l, command );
            addMetadataToList( dir.getParentFile(), hasCommand, l, command );

            return l;
        }
        else
        {
            return Collections.singletonList( line );
        }
    }

    private static void addMetadataToList( File dir, boolean hasCommand, List<String> l, String command )
    {
        if ( dir.exists() && dir.isDirectory() )
        {
            String[] files = dir.list( new FilenameFilter()
            {
                public boolean accept( File dir, String name )
                {
                    return name.startsWith( "maven-metadata" ) && name.endsWith( ".xml" );

                }
            } );

            for ( int i = 0; i < files.length; i++ )
            {
                if ( hasCommand )
                {
                    l.add( command + " " + new File( dir, files[i] ).getPath() );
                }
                else
                {
                    l.add( new File( dir, files[i] ).getPath() );
                }
            }
        }
    }

    private String getArtifactPath( String artifact )
    {
        StringTokenizer tok = new StringTokenizer( artifact, ":" );
        if ( tok.countTokens() != 4 )
        {
            throw new IllegalArgumentException( "Artifact must have 4 tokens: '" + artifact + "'" );
        }

        String[] a = new String[4];
        for ( int i = 0; i < 4; i++ )
        {
            a[i] = tok.nextToken();
        }

        String org = a[0];
        String name = a[1];
        String version = a[2];
        String ext = a[3];
        return getArtifactPath( org, name, version, ext, null );
    }

    public static String getArtifactPath( String org, String name, String version, String ext )
    {
        return getArtifactPath( org, name, version, ext, null );
    }

    public static String getArtifactPath( String org, String name, String version, String ext, String classifier )
    {
        if ( "maven-plugin".equals( ext ) )
        {
            ext = "jar";
        }
        if ( "coreit-artifact".equals( ext ) )
        {
            ext = "jar";
            classifier = "it";
        }
        if ( "test-jar".equals( ext ) )
        {
            ext = "jar";
            classifier = "tests";
        }

        String repositoryPath;
        if ( "legacy".equals( localRepoLayout ) )
        {
            repositoryPath = org + "/" + ext + "s/" + name + "-" + version + "." + ext;
        }
        else if ( "default".equals( localRepoLayout ) )
        {
            repositoryPath = org.replace( '.', '/' );
            repositoryPath = repositoryPath + "/" + name + "/" + version;
            repositoryPath = repositoryPath + "/" + name + "-" + version;
            if ( classifier != null )
            {
                repositoryPath = repositoryPath + "-" + classifier;
            }
            repositoryPath = repositoryPath + "." + ext;
        }
        else
        {
            throw new IllegalStateException( "Unknown layout: " + localRepoLayout );
        }

        return localRepo + "/" + repositoryPath;
    }

    public List<String> getArtifactFileNameList( String org, String name, String version, String ext )
    {
        return getArtifactFileNameList( org, name, version, ext, null );
    }

    public static List<String> getArtifactFileNameList( String org, String name, String version, String ext,
                                                        String classifier )
    {
        List<String> files = new ArrayList<String>();
        String artifactPath = getArtifactPath( org, name, version, ext, classifier );
        File dir = new File( artifactPath );
        files.add( artifactPath );
        addMetadataToList( dir, false, files, null );
        addMetadataToList( dir.getParentFile(), false, files, null );
        return files;
    }

    /**
     * Gets the path to the local artifact metadata. Note that the method does not check whether the returned path
     * actually points to existing metadata.
     * 
     * @param gid The group id, must not be <code>null</code>.
     * @param aid The artifact id, must not be <code>null</code>.
     * @param version The artifact version, must not be <code>null</code>.
     * @return The (absolute) path to the local artifact metadata, never <code>null</code>.
     */
    public String getArtifactMetadataPath( String gid, String aid, String version )
    {
        StringBuffer buffer = new StringBuffer( 256 );

        buffer.append( localRepo );
        buffer.append( '/' );

        if ( "default".equals( localRepoLayout ) )
        {
            buffer.append( gid.replace( '.', '/' ) );
            buffer.append( '/' );
            buffer.append( aid );
            buffer.append( '/' );
            if ( version != null )
            {
                buffer.append( version );
                buffer.append( '/' );
            }
            buffer.append( "maven-metadata-local.xml" );
        }
        else
        {
            throw new IllegalStateException( "Unsupported repository layout: " + localRepoLayout );
        }

        return buffer.toString();
    }

    /**
     * Gets the path to the local artifact metadata. Note that the method does not check whether the returned path
     * actually points to existing metadata.
     * 
     * @param gid The group id, must not be <code>null</code>.
     * @param aid The artifact id, must not be <code>null</code>.
     * @return The (absolute) path to the local artifact metadata, never <code>null</code>.
     */
    public String getArtifactMetadataPath( String gid, String aid )
    {
        return getArtifactMetadataPath( gid, aid, null );
    }

    public void executeHook( String filename )
        throws VerificationException
    {
        try
        {
            File f = new File( getBasedir(), filename );

            if ( !f.exists() )
            {
                return;
            }

            List<String> lines = loadFile( f, true );

            for ( Iterator<String> i = lines.iterator(); i.hasNext(); )
            {
                String line = resolveCommandLineArg( i.next() );

                executeCommand( line );
            }
        }
        catch ( VerificationException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new VerificationException( e );
        }
    }

    private void executeCommand( String line )
        throws VerificationException
    {
        int index = line.indexOf( " " );

        String cmd;

        String args = null;

        if ( index >= 0 )
        {
            cmd = line.substring( 0, index );

            args = line.substring( index + 1 );
        }
        else
        {
            cmd = line;
        }

        if ( "rm".equals( cmd ) )
        {
            System.out.println( "Removing file: " + args );

            File f = new File( args );

            if ( f.exists() && !f.delete() )
            {
                throw new VerificationException( "Error removing file - delete failed" );
            }
        }
        else if ( "rmdir".equals( cmd ) )
        {
            System.out.println( "Removing directory: " + args );

            try
            {
                File f = new File( args );

                FileUtils.deleteDirectory( f );
            }
            catch ( IOException e )
            {
                throw new VerificationException( "Error removing directory - delete failed" );
            }
        }
        else if ( "svn".equals( cmd ) )
        {
            launchSubversion( line, getBasedir() );
        }
        else
        {
            throw new VerificationException( "unknown command: " + cmd );
        }
    }

    public static void launchSubversion( String line, String basedir )
        throws VerificationException
    {
        try
        {
            Commandline cli = new Commandline( line );

            cli.setWorkingDirectory( basedir );

            Writer logWriter = new FileWriter( new File( basedir, LOG_FILENAME ) );

            StreamConsumer out = new WriterStreamConsumer( logWriter );

            StreamConsumer err = new WriterStreamConsumer( logWriter );

            System.out.println( "Command: " + Commandline.toString( cli.getCommandline() ) );

            int ret = CommandLineUtils.executeCommandLine( cli, out, err );

            logWriter.close();

            if ( ret > 0 )
            {
                System.err.println( "Exit code: " + ret );

                throw new VerificationException();
            }
        }
        catch ( CommandLineException e )
        {
            throw new VerificationException( e );
        }
        catch ( IOException e )
        {
            throw new VerificationException( e );
        }
    }

    private static String retrieveLocalRepo( String settingsXmlPath )
        throws VerificationException
    {
        UserModelReader userModelReader = new UserModelReader();

        String userHome = System.getProperty( "user.home" );

        File userXml;

        String repo = null;

        if ( settingsXmlPath != null )
        {
            System.out.println( "Using settings from " + settingsXmlPath );
            userXml = new File( settingsXmlPath );
        }
        else
        {
            userXml = new File( userHome, ".m2/settings.xml" );
        }

        if ( userXml.exists() )
        {
            userModelReader.parse( userXml );

            String localRepository = userModelReader.getLocalRepository();
            if ( localRepository != null )
            {
                repo = new File( localRepository ).getAbsolutePath();
            }
        }

        return repo;
    }

    public static void deleteArtifact( String org, String name, String version, String ext )
        throws IOException
    {
        deleteArtifact( org, name, version, ext, null );
    }

    public static void deleteArtifact( String org, String name, String version, String ext, String classifier )
        throws IOException
    {
        List<String> files = getArtifactFileNameList( org, name, version, ext, classifier );
        for ( Iterator<String> i = files.iterator(); i.hasNext(); )
        {
            String fileName = i.next();
            FileUtils.forceDelete( new File( fileName ) );
        }
    }

    /**
     * Deletes all artifacts in the specified group id from the local repository.
     * 
     * @param gid The group id whose artifacts should be deleted, must not be <code>null</code>.
     * @throws IOException If the artifacts could not be deleted.
     * @since 1.2
     */
    public static void deleteArtifacts( String gid )
        throws IOException
    {
        String path;
        if ( "default".equals( localRepoLayout ) )
        {
            path = gid.replace( '.', '/' );
        }
        else if ( "legacy".equals( localRepoLayout ) )
        {
            path = gid;
        }
        else
        {
            throw new IllegalStateException( "Unsupported repository layout: " + localRepoLayout );
        }

        FileUtils.deleteDirectory( new File( localRepo, path ) );
    }

    /**
     * Deletes the specified directory.
     * 
     * @param path The path to the directory to delete, relative to the base directory, must not be <code>null</code>.
     * @throws IOException If the directory could not be deleted.
     * @since 1.2
     */
    public void deleteDirectory( String path )
        throws IOException
    {
        FileUtils.deleteDirectory( new File( getBasedir(), path ) );
    }

    /**
     * Writes a text file with the specified contents. The contents will be encoded using UTF-8.
     * 
     * @param path The path to the file, relative to the base directory, must not be <code>null</code>.
     * @param contents The contents to write, must not be <code>null</code>.
     * @throws IOException If the file could not be written.
     * @since 1.2
     */
    public void writeFile( String path, String contents )
        throws IOException
    {
        FileUtils.fileWrite( new File( getBasedir(), path ).getAbsolutePath(), "UTF-8", contents );
    }

    /**
     * Filters a text file by replacing some user-defined tokens.
     * 
     * @param srcPath The path to the input file, relative to the base directory, must not be <code>null</code>.
     * @param dstPath The path to the output file, relative to the base directory and possibly equal to the input file,
     *            must not be <code>null</code>.
     * @param fileEncoding The file encoding to use, may be <code>null</code> or empty to use the platform's default
     *            encoding.
     * @param filterProperties The mapping from tokens to replacement values, must not be <code>null</code>.
     * @return The path to the filtered output file, never <code>null</code>.
     * @throws IOException If the file could not be filtered.
     * @since 1.2
     */
    public File filterFile( String srcPath, String dstPath, String fileEncoding, Map<String, String> filterProperties )
        throws IOException
    {
        File srcFile = new File( getBasedir(), srcPath );
        String data = FileUtils.fileRead( srcFile, fileEncoding );

        for ( Iterator<String> it = filterProperties.keySet().iterator(); it.hasNext(); )
        {
            String token = it.next();
            String value = String.valueOf( filterProperties.get( token ) );
            data = StringUtils.replace( data, token, value );
        }

        File dstFile = new File( getBasedir(), dstPath );
        FileUtils.fileWrite( dstFile.getPath(), fileEncoding, data );

        return dstFile;
    }

    /**
     * Gets a new copy of the default filter properties. These default filter properties map the tokens "@basedir@" and
     * "@baseurl@" to the test's base directory and its base <code>file:</code> URL, respectively.
     * 
     * @return The (modifiable) map with the default filter properties, never <code>null</code>.
     * @since 1.2
     */
    public Properties newDefaultFilterProperties()
    {
        Properties filterProperties = new Properties();

        String basedir = new File( getBasedir() ).getAbsolutePath();
        filterProperties.put( "@basedir@", basedir );

        /*
         * NOTE: Maven fails to properly handle percent-encoded "file:" URLs (WAGON-111) so don't use File.toURI() here
         * and just do it the simple way.
         */
        String baseurl = basedir;
        if ( !baseurl.startsWith( "/" ) )
        {
            baseurl = '/' + baseurl;
        }
        baseurl = "file://" + baseurl.replace( '\\', '/' );
        filterProperties.put( "@baseurl@", baseurl );

        return filterProperties;
    }

    public void assertFilePresent( String file )
    {
        try
        {
            verifyExpectedResult( file, true );
        }
        catch ( VerificationException e )
        {
            Assert.fail( e.getMessage() );
        }
    }

    /**
     * Check that given file's content matches an regular expression. Note this method also checks that the file exists
     * and is readable.
     * 
     * @param file the file to check.
     * @param regex a regular expression.
     * @see Pattern
     */
    public void assertFileMatches( String file, String regex )
    {
        assertFilePresent( file );
        try
        {
            String content = FileUtils.fileRead( file );
            if ( !Pattern.matches( regex, content ) )
            {
                Assert.fail( "Content of " + file + " does not match " + regex );
            }
        }
        catch ( IOException e )
        {
            Assert.fail( e.getMessage() );
        }
    }

    public void assertFileNotPresent( String file )
    {
        try
        {
            verifyExpectedResult( file, false );
        }
        catch ( VerificationException e )
        {
            Assert.fail( e.getMessage() );
        }
    }

    private void verifyArtifactPresence( boolean wanted, String org, String name, String version, String ext,
                                         String classifier )
    {
        List<String> files = getArtifactFileNameList( org, name, version, ext, classifier );
        for ( Iterator<String> i = files.iterator(); i.hasNext(); )
        {
            String fileName = i.next();
            try
            {
                verifyExpectedResult( fileName, wanted );
            }
            catch ( VerificationException e )
            {
                Assert.fail( e.getMessage() );
            }
        }
    }

    public void assertArtifactPresent( String org, String name, String version, String ext )
    {
        assertArtifactPresent( org, name, version, ext, null );
    }

    public void assertArtifactPresent( String org, String name, String version, String ext, String classifier )
    {
        verifyArtifactPresence( true, org, name, version, ext, classifier );
    }

    public void assertArtifactNotPresent( String org, String name, String version, String ext )
    {
        assertArtifactNotPresent( org, name, version, ext, null );
    }

    public void assertArtifactNotPresent( String org, String name, String version, String ext, String classifier )
    {
        verifyArtifactPresence( false, org, name, version, ext, classifier );
    }

    private void verifyExpectedResult( String line )
        throws VerificationException
    {
        boolean wanted = true;
        if ( line.startsWith( "!" ) )
        {
            line = line.substring( 1 );
            wanted = false;
        }

        verifyExpectedResult( line, wanted );
    }

    private void verifyExpectedResult( String line, boolean wanted )
        throws VerificationException
    {
        if ( line.indexOf( "!/" ) > 0 )
        {
            String urlString = "jar:file:" + getBasedir() + "/" + line;

            InputStream is = null;
            try
            {
                URL url = new URL( urlString );

                is = url.openStream();

                if ( is == null )
                {
                    if ( wanted )
                    {
                        throw new VerificationException( "Expected JAR resource was not found: " + line );
                    }
                }
                else
                {
                    if ( !wanted )
                    {
                        throw new VerificationException( "Unwanted JAR resource was found: " + line );
                    }
                }
            }
            catch ( MalformedURLException e )
            {
                throw new VerificationException( "Error looking for JAR resource", e );
            }
            catch ( IOException e )
            {
                throw new VerificationException( "Error looking for JAR resource", e );
            }
            finally
            {
                if ( is != null )
                {
                    try
                    {
                        is.close();
                    }
                    catch ( IOException e )
                    {
                        System.err.println( "WARN: error closing stream: " + e );
                    }
                }
            }
        }
        else
        {
            File expectedFile = new File( line );

            // NOTE: On Windows, a path with a leading (back-)slash is relative to the current drive
            if ( !expectedFile.isAbsolute() && !expectedFile.getPath().startsWith( File.separator ) )
            {
                expectedFile = new File( getBasedir(), line );
            }

            if ( line.indexOf( '*' ) > -1 )
            {
                File parent = expectedFile.getParentFile();

                if ( !parent.exists() )
                {
                    if ( wanted )
                    {
                        throw new VerificationException( "Expected file pattern was not found: "
                            + expectedFile.getPath() );
                    }
                }
                else
                {
                    String shortNamePattern = expectedFile.getName().replaceAll( "\\*", ".*" );

                    String[] candidates = parent.list();

                    boolean found = false;

                    if ( candidates != null )
                    {
                        for ( int i = 0; i < candidates.length; i++ )
                        {
                            if ( candidates[i].matches( shortNamePattern ) )
                            {
                                found = true;
                                break;
                            }
                        }
                    }

                    if ( !found && wanted )
                    {
                        throw new VerificationException( "Expected file pattern was not found: "
                            + expectedFile.getPath() );
                    }
                    else if ( found && !wanted )
                    {
                        throw new VerificationException( "Unwanted file pattern was found: " + expectedFile.getPath() );
                    }
                }
            }
            else
            {
                if ( !expectedFile.exists() )
                {
                    if ( wanted )
                    {
                        throw new VerificationException( "Expected file was not found: " + expectedFile.getPath() );
                    }
                }
                else
                {
                    if ( !wanted )
                    {
                        throw new VerificationException( "Unwanted file was found: " + expectedFile.getPath() );
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void executeGoal( String goal )
        throws VerificationException
    {
        executeGoal( goal, new HashMap<String, String>() );
    }

    public void executeGoal( String goal, Map<String, String> envVars )
        throws VerificationException
    {
        executeGoals( Arrays.asList( new String[] { goal } ), envVars );
    }

    public void executeGoals( List<String> goals )
        throws VerificationException
    {
        executeGoals( goals, new HashMap<String, String>() );
    }

    public String getExecutable()
    {
        // Use a strategy for finding the maven executable, John has a simple method like this
        // but a little strategy + chain of command would be nicer.

        String mavenHome = defaultMavenHome;

        if ( mavenHome != null )
        {
            return mavenHome + "/bin/mvn";
        }
        else
        {
            File f = new File( System.getProperty( "user.home" ), "m2/bin/mvn" );

            if ( f.exists() )
            {
                return f.getAbsolutePath();
            }
            else
            {
                return "mvn";
            }
        }
    }

    public void executeGoals( List<String> goals, Map<String, String> envVars )
        throws VerificationException
    {
        if ( goals.size() == 0 )
        {
            throw new VerificationException( "No goals specified" );
        }

        List<String> allGoals = new ArrayList<String>();

        if ( autoclean )
        {
            /*
             * NOTE: Neither test lifecycle binding nor prefix resolution here but call the goal directly.
             */
            allGoals.add( "org.apache.maven.plugins:maven-clean-plugin:clean" );
        }

        allGoals.addAll( goals );

        Commandline cli = null;
        int ret;

        File logFile = new File( getBasedir(), getLogFileName() );
        try
        {
            cli = createCommandLine();

            for ( Iterator<String> i = envVars.keySet().iterator(); i.hasNext(); )
            {
                String key = i.next();

                cli.addEnvironment( key, envVars.get( key ) );

                /*
                 * What was the point of this? It doesn't work on windows. try { FileUtils.fileWrite( "/tmp/foo.txt",
                 * "setting envar[ " + key + " = " + envVars.get( key ) ); } catch ( IOException e ) {
                 * e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates. }
                 */

                // System.out.println();
            }

            if ( envVars.get( "JAVA_HOME" ) == null )
            {
                cli.addEnvironment( "JAVA_HOME", System.getProperty( "java.home" ) );
            }

            cli.addEnvironment( "MAVEN_TERMINATE_CMD", "on" );

            cli.setWorkingDirectory( getBasedir() );

            for ( Iterator<String> it = cliOptions.iterator(); it.hasNext(); )
            {
                String key = String.valueOf( it.next() );

                String resolvedArg = resolveCommandLineArg( key );

                cli.createArgument().setLine( resolvedArg );
            }

            cli.createArgument().setValue( "-e" );

            cli.createArgument().setValue( "--batch-mode" );

            if ( this.mavenDebug )
            {
                cli.createArgument().setValue( "--debug" );
            }

            for ( Iterator<String> i = systemProperties.stringPropertyNames().iterator(); i.hasNext(); )
            {
                String key = (String) i.next();
                String value = systemProperties.getProperty( key );
                cli.createArgument().setValue( "-D" + key + "=" + value );
            }

            /*
             * NOTE: Unless explicitly requested by the caller, the forked builds should use the current local
             * repository. Otherwise, the forked builds would in principle leave the sandbox environment which has been
             * setup for the current build. In particular, using "maven.repo.local" will make sure the forked builds use
             * the same local repo as the parent build even if a custom user settings is provided.
             */
            boolean useMavenRepoLocal =
                Boolean.valueOf( verifierProperties.getProperty( "use.mavenRepoLocal", "true" ) ).booleanValue();

            if ( useMavenRepoLocal )
            {
                cli.createArgument().setValue( "-Dmaven.repo.local=" + localRepo );
            }

            for ( Iterator<String> i = allGoals.iterator(); i.hasNext(); )
            {
                cli.createArgument().setValue( i.next() );
            }

            // System.out.println( "Command: " + Commandline.toString( cli.getCommandline() ) );

            ret = runCommandLine( cli, logFile );
        }
        catch ( CommandLineException e )
        {
            throw new VerificationException( "Failed to execute Maven: " + cli, e );
        }
        catch ( IOException e )
        {
            throw new VerificationException( e );
        }

        if ( ret > 0 )
        {
            System.err.println( "Exit code: " + ret );

            throw new VerificationException( "Exit code was non-zero: " + ret + "; command line and log = \n" + cli
                + "\n" + getLogContents( logFile, 75 ) );
        }
    }

    public String getMavenVersion()
        throws VerificationException
    {
        Commandline cmd = createCommandLine();
        cmd.addArguments( new String[] { "--version" } );

        File log;
        try
        {
            log = File.createTempFile( "maven", "log" );
        }
        catch ( IOException e )
        {
            throw new VerificationException( "Error creating temp file", e );
        }

        try
        {
            runCommandLine( cmd, log );
        }
        catch ( CommandLineException e )
        {
            throw new VerificationException( "Error running commandline " + cmd.toString(), e );
        }
        catch ( IOException e )
        {
            throw new VerificationException( "IO Error communicating with commandline " + cmd.toString(), e );
        }

        String version = null;

        List<String> logLines = loadFile( log, false );
        log.delete();

        for ( Iterator<String> it = logLines.iterator(); version == null && it.hasNext(); )
        {
            String line = it.next();

            final String MAVEN_VERSION = "Maven version: ";

            // look out for "Maven version: 3.0-SNAPSHOT built on unknown"
            if ( line.regionMatches( true, 0, MAVEN_VERSION, 0, MAVEN_VERSION.length() ) )
            {
                version = line.substring( MAVEN_VERSION.length() ).trim();
                if ( version.indexOf( ' ' ) >= 0 )
                {
                    version = version.substring( 0, version.indexOf( ' ' ) );
                }
            }

            final String NEW_MAVEN_VERSION = "Apache Maven ";

            // look out for "Apache Maven 2.1.0-M2-SNAPSHOT (rXXXXXX; date)"
            if ( line.regionMatches( true, 0, NEW_MAVEN_VERSION, 0, NEW_MAVEN_VERSION.length() ) )
            {
                version = line.substring( NEW_MAVEN_VERSION.length() ).trim();
                if ( version.indexOf( ' ' ) >= 0 )
                {
                    version = version.substring( 0, version.indexOf( ' ' ) );
                }
            }
        }

        if ( version == null )
        {
            throw new VerificationException(
                                             "Illegal maven output: String 'Maven version: ' not found in the following output:\n"
                                                 + StringUtils.join( logLines.iterator(), "\n" ) );
        }
        else
        {
            return version;
        }
    }

    private Commandline createCommandLine()
    {
        Commandline cmd = new Commandline();
        String executable = getExecutable();
        if ( executable.endsWith( "/bin/mvn" ) )
        {
            cmd.addEnvironment( "M2_HOME", executable.substring( 0, executable.length() - 8 ) );
        }
        cmd.setExecutable( executable );
        return cmd;
    }

    private int runCommandLine( Commandline cli, File logFile )
        throws CommandLineException, IOException
    {
        if ( forkJvm )
        {
            Writer logWriter = new FileWriter( logFile );

            logWriter.append( Arrays.toString( cli.getShellCommandline() ) );

            StreamConsumer out = new WriterStreamConsumer( logWriter );

            StreamConsumer err = new WriterStreamConsumer( logWriter );

            try
            {
                return CommandLineUtils.executeCommandLine( cli, out, err );
            }
            finally
            {
                logWriter.close();
            }
        }

        String mavenHome = defaultMavenHome;

        if ( mavenHome == null )
        {
            mavenHome = System.getProperty( "user.home" ) + "/local/apache-maven-2.1-SNAPSHOT";
        }

        File coreDir = new File( mavenHome, "core/boot" );
        File[] files = coreDir.listFiles();
        File classWorldFile = null;
        for ( int i = 0; files != null && i < files.length; i++ )
        {
            if ( files[i].getName().indexOf( "plexus-classworlds" ) >= 0 )
            {
                classWorldFile = files[i];
                break;
            }
        }

        if ( classWorldFile == null )
        {
            throw new CommandLineException( "Cannot find plexus-classworlds in " + coreDir );
        }

        URLClassLoader cl;

        try
        {
            cl = new URLClassLoader( new URL[] { classWorldFile.toURI().toURL() }, null );
        }
        catch ( MalformedURLException e )
        {
            throw new CommandLineException( "Cannot conver to url: " + classWorldFile, e );
        }

        class ExitSecurityException
            extends SecurityException
        {

            private static final long serialVersionUID = 5650869246591762064L;

            private int status;

            public ExitSecurityException( int status )
            {
                this.status = status;
            }

            public int getStatus()
            {
                return status;
            }
        }

        try
        {
            Class<?> c = cl.loadClass( "org.codehaus.plexus.classworlds.launcher.Launcher" );

            Method m = c.getMethod( "mainWithExitCode", new Class[] { String[].class } );

            SecurityManager oldSm = System.getSecurityManager();

            try
            {
                System.setSecurityManager( new SecurityManager()
                {
                    public void checkPermission( Permission perm )
                    {
                        // ok
                    }

                    public void checkExit( int status )
                    {
                        throw new ExitSecurityException( status );
                    }
                } );
            }
            catch ( AccessControlException e )
            {
                throw new CommandLineException( "Error isntalling securitymanager", e );
            }

            cli.createArgument().setValue( "-f" );
            cli.createArgument().setValue( cli.getWorkingDirectory().getAbsolutePath() + "/pom.xml" );

            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;

            String oldCwConf = System.getProperty( "classworlds.conf" );
            String oldMavenHome = System.getProperty( "maven.home" );

            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

            try
            {
                Thread.currentThread().setContextClassLoader( cl );// ClassLoader.getSystemClassLoader() );
                FileOutputStream logWriter = new FileOutputStream( logFile );
                System.setOut( new PrintStream( logWriter ) );
                System.setErr( new PrintStream( logWriter ) );

                System.setProperty( "classworlds.conf", new File( mavenHome, "bin/m2.conf" ).getAbsolutePath() );
                System.setProperty( "maven.home", mavenHome );

                return ( (Integer) m.invoke( null, new Object[] { cli.getArguments() } ) ).intValue();
            }
            catch ( ExitSecurityException e )
            {
                oldOut.println( "exit security exception caught: status=" + e.getStatus() );
                return e.getStatus();
            }
            finally
            {
                System.setOut( oldOut );
                System.setErr( oldErr );
                if ( oldCwConf == null )
                {
                    System.getProperties().remove( "classworlds.conf" );
                }
                else
                {
                    System.setProperty( "classworlds.conf", oldCwConf );
                }
                if ( oldMavenHome == null )
                {
                    System.getProperties().remove( "maven.home" );
                }
                else
                {
                    System.setProperty( "maven.home", oldMavenHome );
                }
                Thread.currentThread().setContextClassLoader( oldCl );
                System.setSecurityManager( oldSm );
            }
        }
        catch ( ClassNotFoundException e )
        {
            throw new CommandLineException( "Cannot load classworlds launcher", e );
        }
        catch ( NoSuchMethodException e )
        {
            throw new CommandLineException( "Cannot find classworlds launcher's main method", e );
        }
        catch ( IllegalArgumentException e )
        {
            throw new CommandLineException( "Error executing classworlds launcher's main method", e );
        }
        catch ( InvocationTargetException e )
        {
            if ( e.getCause() instanceof ExitSecurityException )
            {
                return ( (ExitSecurityException) e.getCause() ).getStatus();
            }
            throw new CommandLineException( "Error executing classworlds launcher's main method", e );
        }
        catch ( IllegalAccessException e )
        {
            throw new CommandLineException( "Error executing classworlds launcher's main method", e );
        }
    }

    private static String getLogContents( File logFile, int lines )
    {
        try
        {
            String[] content = FileUtils.fileRead( logFile ).split( "\n" );
            if ( content.length > lines )
            {
                content = Arrays.copyOfRange( content, content.length - lines, content.length - 1 );
            }
            return "Full log at: " + logFile + "\n" + Lambda.aggregate( content, new Concat( "\n" ) ).toString();
        }
        catch ( IOException e )
        {
            // ignore
            return "(Error reading log contents: " + e.getMessage() + "): " + logFile;
        }
    }

    private String resolveCommandLineArg( String key )
    {
        String result = key.replaceAll( "\\$\\{basedir\\}", getBasedir() );
        if ( result.indexOf( "\\\\" ) >= 0 )
        {
            result = result.replaceAll( "\\\\", "\\" );
        }
        result = result.replaceAll( "\\/\\/", "\\/" );

        return result;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void findLocalRepo( String settingsFile )
        throws VerificationException
    {
        if ( localRepo == null )
        {
            localRepo = System.getProperty( "maven.repo.local" );
        }

        if ( localRepo == null )
        {
            localRepo = retrieveLocalRepo( settingsFile );
        }

        if ( localRepo == null )
        {
            localRepo = System.getProperty( "user.home" ) + "/.m2/repository";
        }

        File repoDir = new File( localRepo );

        if ( !repoDir.exists() )
        {
            repoDir.mkdirs();
        }

        // normalize path
        localRepo = repoDir.getAbsolutePath();
    }

    public void assertArtifactContents( String org, String artifact, String version, String type, String classifier,
                                        String contents )
        throws IOException
    {
        String fileName = getArtifactPath( org, artifact, version, type, classifier );
        Assert.assertEquals( contents, FileUtils.fileRead( fileName ) );
    }

    static class UserModelReader
        extends DefaultHandler
    {
        private String localRepository;

        private StringBuffer currentBody = new StringBuffer();

        public void parse( File file )
            throws VerificationException
        {
            try
            {
                SAXParserFactory saxFactory = SAXParserFactory.newInstance();

                SAXParser parser = saxFactory.newSAXParser();

                InputSource is = new InputSource( new FileInputStream( file ) );

                parser.parse( is, this );
            }
            catch ( FileNotFoundException e )
            {
                throw new VerificationException( e );
            }
            catch ( IOException e )
            {
                throw new VerificationException( e );
            }
            catch ( ParserConfigurationException e )
            {
                throw new VerificationException( e );
            }
            catch ( SAXException e )
            {
                throw new VerificationException( e );
            }
        }

        public void warning( SAXParseException spe )
        {
            printParseError( "Warning", spe );
        }

        public void error( SAXParseException spe )
        {
            printParseError( "Error", spe );
        }

        public void fatalError( SAXParseException spe )
        {
            printParseError( "Fatal Error", spe );
        }

        private final void printParseError( String type, SAXParseException spe )
        {
            System.err.println( type + " [line " + spe.getLineNumber() + ", row " + spe.getColumnNumber() + "]: "
                + spe.getMessage() );
        }

        public String getLocalRepository()
        {
            return localRepository;
        }

        public void characters( char[] ch, int start, int length )
            throws SAXException
        {
            currentBody.append( ch, start, length );
        }

        public void endElement( String uri, String localName, String rawName )
            throws SAXException
        {
            if ( "localRepository".equals( rawName ) )
            {
                if ( notEmpty( currentBody.toString() ) )
                {
                    localRepository = currentBody.toString().trim();
                }
                else
                {
                    throw new SAXException( "Invalid mavenProfile entry. Missing one or more "
                        + "fields: {localRepository}." );
                }
            }

            currentBody = new StringBuffer();
        }

        private boolean notEmpty( String test )
        {
            return test != null && test.trim().length() > 0;
        }

        public void reset()
        {
            currentBody = null;
            localRepository = null;
        }
    }

    public List<String> getCliOptions()
    {
        return cliOptions;
    }

    public void setCliOptions( List<String> cliOptions )
    {
        this.cliOptions = cliOptions;
    }

    public Properties getSystemProperties()
    {
        return systemProperties;
    }

    public void setSystemProperties( Properties systemProperties )
    {
        this.systemProperties = systemProperties;
    }

    public Properties getVerifierProperties()
    {
        return verifierProperties;
    }

    public void setVerifierProperties( Properties verifierProperties )
    {
        this.verifierProperties = verifierProperties;
    }

    public boolean isAutoclean()
    {
        return autoclean;
    }

    public void setAutoclean( boolean autoclean )
    {
        this.autoclean = autoclean;
    }

    public String getBasedir()
    {
        return basedir;
    }

    /**
     * Gets the name of the file used to log build output.
     * 
     * @return The name of the log file, relative to the base directory, never <code>null</code>.
     * @since 1.2
     */
    public String getLogFileName()
    {
        return this.logFileName;
    }

    /**
     * Sets the name of the file used to log build output.
     * 
     * @param logFileName The name of the log file, relative to the base directory, must not be empty or
     *            <code>null</code>.
     * @since 1.2
     */
    public void setLogFileName( String logFileName )
    {
        if ( StringUtils.isEmpty( logFileName ) )
        {
            throw new IllegalArgumentException( "log file name unspecified" );
        }
        this.logFileName = logFileName;
    }

    public void setDebug( boolean debug )
    {
        this.debug = debug;
    }

    public boolean isMavenDebug()
    {
        return mavenDebug;
    }

    public void setMavenDebug( boolean mavenDebug )
    {
        this.mavenDebug = mavenDebug;
    }

}
