/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.tests;

import static org.sonatype.flexmojos.util.PathUtil.file;
import static org.sonatype.flexmojos.util.PathUtil.path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.maven.it.VerificationException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.ModelParseException;
import org.apache.maven.model.io.ModelWriter;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.sonatype.flexmojos.test.FMVerifier;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.sonatype.flexmojos.util.OSUtils;
import org.sonatype.flexmojos.util.PathUtil;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class AbstractFlexMojosTests
{

    protected static File projectsSource;

    protected static File projectsWorkdir;

    private static Properties props;

    private static File mavenHome;

    protected static PlexusContainer container;

    private static File flashplayer;

    private static final ReadWriteLock copyProjectLock = new ReentrantReadWriteLock();

    private static final ReadWriteLock downloadArtifactsLock = new ReentrantReadWriteLock();

    @BeforeSuite( alwaysRun = true )
    public static void initFolders()
        throws Exception
    {
        if ( props != null )
        {
            return;
        }
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
        mavenHome = new File( getProperty( "fake-maven" ) );

        File mvn = new File( mavenHome, "bin/mvn" );
        updateMavenMemory( mvn, "\nMAVEN_OPTS=\"-Xmx512M -Duser.language=en -Duser.region=US\"\n" );
        File mvnBat = new File( mavenHome, "bin/mvn.bat" );
        updateMavenMemory( mvnBat, "\nset MAVEN_OPTS=-Xmx512M -Duser.language=en -Duser.region=US\n" );

        container = new DefaultPlexusContainer();
    }

    @BeforeSuite( dependsOnMethods = { "initFolders" }, alwaysRun = true )
    public static void addEmmaToClasshPath()
        throws Exception
    {
        File repo = new File( getProperty( "fake-repo" ) );
        addCobertura( new File( repo, "org/sonatype/flexmojos/flexmojos-parent/" + getFlexmojosVersion()
            + "/flexmojos-parent-" + getFlexmojosVersion() + ".pom" ) );
        addCobertura( new File( repo, "org/sonatype/flexmojos/flexmojos-maven-plugin/" + getFlexmojosVersion()
            + "/flexmojos-maven-plugin-" + getFlexmojosVersion() + ".pom" ) );
    }

    private static void addCobertura( File fmParentPom )
        throws ComponentLookupException, IOException, ModelParseException
    {
        ModelProcessor builder = container.lookup( ModelProcessor.class );
        Model pom = builder.read( fmParentPom, null );
        Dependency cobertura = new Dependency();
        cobertura.setGroupId( "net.sourceforge.cobertura" );
        cobertura.setArtifactId( "cobertura" );
        cobertura.setVersion( "1.9.4.1" );
        pom.addDependency( cobertura );

        ModelWriter writer = container.lookup( ModelWriter.class );
        writer.write( fmParentPom, null, pom );
    }

    @BeforeSuite
    public static void makeFlashplayerExecutable()
    {
        FMVerifier.setLocalRepo( getProperty( "fake-repo" ) );

        if ( OSUtils.isWindows() )
        {
            flashplayer = file( FMVerifier.getArtifactPath( "com.adobe", "flashplayer", "10.1", "exe" ) );
        }
        else if ( OSUtils.isMacOS() )
        {
            flashplayer = file( FMVerifier.getArtifactPath( "com.adobe", "flashplayer", "10.1", "uexe", "mac" ) );
        }
        else
        {
            flashplayer = file( FMVerifier.getArtifactPath( "com.adobe", "flashplayer", "10.1", "uexe", "linux" ) );
        }
    }

    private static void updateMavenMemory( File mvn, String memString )
        throws IOException
    {
        String mvnContent = org.codehaus.plexus.util.FileUtils.fileRead( mvn );
        if ( mvnContent.contains( memString ) )
        {
            return;
        }
        int i = mvnContent.indexOf( '\n' );
        mvnContent = mvnContent.substring( 0, i ) + memString + mvnContent.substring( i );
        org.codehaus.plexus.util.FileUtils.fileWrite( mvn.getAbsolutePath(), mvnContent );
    }

    protected static synchronized String getProperty( String key )
    {
        return props.getProperty( key );
    }

    protected FMVerifier test( File projectDirectory, String goal, String... args )
        throws VerificationException
    {
        FMVerifier verifier = getVerifier( projectDirectory, args );
        verifier.getCliOptions().addAll( Arrays.asList( args ) );
        verifier.executeGoal( goal );
        // TODO there are some errors logged, but they are not my concern
        // verifier.verifyErrorFreeLog();
        return verifier;
    }

    protected FMVerifier getVerifier( File projectDirectory )
        throws VerificationException
    {
        return getVerifier( projectDirectory, new String[0] );
    }

    protected FMVerifier getVerifier( File projectDirectory, String... args )
        throws VerificationException
    {
        System.setProperty( "maven.home", mavenHome.getAbsolutePath() );
        // System.setProperty( "maven.home", "" );

        if ( new File( projectDirectory, "pom.xml" ).exists() )
        {
            downloadArtifactsLock.writeLock().lock();
            try
            {
                FMVerifier verifier = new FMVerifier( projectDirectory.getAbsolutePath() );
                verifier.getVerifierProperties().put( "use.mavenRepoLocal", "true" );
                verifier.setAutoclean( false );
                verifier.getCliOptions().add( "-npu" );
                verifier.getCliOptions().add( "-B" );
                verifier.getCliOptions().addAll( Arrays.asList( args ) );
                // verifier.getCliOptions().add( "-X" );
                verifier.setLogFileName( getTestName() + ".resolve.log" );
                verifier.executeGoal( "dependency:go-offline" );
            }
            catch ( Throwable t )
            {
                t.printStackTrace();
                // this is not a real issue
            }
            finally
            {
                downloadArtifactsLock.writeLock().unlock();
            }
        }

        FMVerifier verifier = new FMVerifier( projectDirectory.getAbsolutePath() );
        // verifier.getCliOptions().add( "-s" + rootFolder.getAbsolutePath() + "/settings.xml" );
        // verifier.getCliOptions().add( "-o" );
        verifier.getCliOptions().add( "-npu" );
        verifier.getCliOptions().add( "-B" );
        verifier.getCliOptions().add( "-X" );
        verifier.getCliOptions().add( "-Dflex.coverage=true" );
        verifier.getCliOptions().add( "-Dflex.coverageOverwriteSourceRoots=" + getSourceRoots() );
        verifier.getVerifierProperties().put( "use.mavenRepoLocal", "true" );
        Properties sysProps = new Properties();
        sysProps.setProperty( "MAVEN_OPTS", "-Xmx512m" );
        sysProps.setProperty( "apparat.threads", "false" );
        sysProps.setProperty( "apparat.debug", "true" );
        verifier.setSystemProperties( sysProps );
        verifier.setLogFileName( getTestName() + ".log" );
        verifier.setAutoclean( false );

        return verifier;
    }

    private String getSourceRoots()
    {
        return getPath( "flexmojos-unittest-advancedflex" ) + "," + getPath( "flexmojos-unittest-asunit" ) + ","
            + getPath( "flexmojos-unittest-flexunit" ) + "," + getPath( "flexmojos-unittest-flexunit4" ) + ","
            + getPath( "flexmojos-unittest-fluint" ) + "," + getPath( "flexmojos-unittest-funit" ) + ","
            + getPath( "flexmojos-unittest-support" );
    }

    private String getPath( String path )
    {
        return PathUtil.path( new File( "../flexmojos-unittest/" + path + "/src/main/flex" ) );
    }

    protected String getTestName()
    {
        StackTraceElement[] stackTrace = new Exception().getStackTrace();
        for ( StackTraceElement stack : stackTrace )
        {
            Class<?> testClass;
            try
            {
                testClass = Class.forName( stack.getClassName() );
            }
            catch ( ClassNotFoundException e )
            {
                // nvm, and should never happen
                continue;
            }
            for ( Method method : testClass.getMethods() )
            {
                if ( method.getName().equals( stack.getMethodName() ) )
                {
                    if ( method.getAnnotation( Test.class ) != null )
                    {
                        return method.getName();
                    }
                }
            }
        }
        return null;
    }

    protected File getProject( String projectName, String... filesToInterpolate )
        throws IOException
    {
        return getProjectCustom( projectName, null, filesToInterpolate );
    }

    protected File getProjectCustom( String projectName, String output, String... filesToInterpolate )
        throws IOException
    {
        if ( filesToInterpolate == null || filesToInterpolate.length == 0 )
        {
            filesToInterpolate = new String[] { "**/pom.xml" };
        }

        copyProjectLock.writeLock().lock();
        try
        {
            File projectFolder = new File( projectsSource, projectName );
            AssertJUnit.assertTrue( "Project " + projectName + " folder not found.\n" + projectFolder.getAbsolutePath(),
                                    projectFolder.isDirectory() );

            if ( output == null )
            {
                output = projectName + "_" + getTestName();
            }
            File destDir = new File( projectsWorkdir, output );

            FileUtils.copyDirectoryStructure( projectFolder, destDir );

            DirectoryScanner scan = new DirectoryScanner();
            scan.setBasedir( destDir );
            scan.setIncludes( filesToInterpolate );
            scan.addDefaultExcludes();
            scan.scan();

            // projects filtering
            for ( String path : scan.getIncludedFiles() )
            {
                File pom = new File( destDir, path );

                String pomContent = FileUtils.fileRead( pom );
                pomContent = pomContent.replace( "%{flexmojos.version}", getFlexmojosVersion() );
                pomContent = pomContent.replace( "%{flex.version}", getFlexSDKVersion() );
                FileUtils.fileWrite( path( pom ), pomContent );
            }

            return destDir;
        }
        finally
        {
            copyProjectLock.writeLock().unlock();
        }
    }

    protected static String getFlexSDKVersion()
    {
        return getProperty( "flex-version" );
    }

    protected static String getFlexmojosVersion()
    {
        return getProperty( "version" );
    }

    protected TestCaseReport getTestReport( File testDir, String testClass )
        throws Exception
    {
        File target = new File( testDir, "target" );

        File testClasses = new File( target, "test-classes" );
        AssertJUnit.assertTrue( "test-classes folder not created!", testClasses.isDirectory() );

        File sureFireReports = new File( target, "surefire-reports" );
        AssertJUnit.assertTrue( "Report folder not created!", sureFireReports.isDirectory() );

        File reportFile = new File( sureFireReports, "TEST-" + testClass + ".xml" );
        AssertJUnit.assertTrue( "Report was not created!", reportFile.isFile() );

        TestCaseReport report = new TestCaseReport( Xpp3DomBuilder.build( new FileReader( reportFile ) ) );

        return report;
    }

    protected Xpp3Dom getFlexConfigReport( FMVerifier verifier, String artifactId )
    {
        return getFlexConfigReport( verifier, artifactId, "1.0-SNAPSHOT" );
    }

    protected Xpp3Dom getFlexConfigReport( FMVerifier verifier, String artifactId, String version )
    {
        File configReport = new File( verifier.getBasedir(), "target/" + artifactId + "-" + version + "-configs.xml" );
        Xpp3Dom configReportDOM;
        try
        {
            configReportDOM = Xpp3DomBuilder.build( ReaderFactory.newXmlReader( configReport ) );
        }
        catch ( Exception e )
        {
            Assert.fail( "Unable to parse \n" + configReport, e );
            throw new RuntimeException( e );
        }

        return configReportDOM;
    }

    protected void assertSeftExit( File main, int expectedExitCode, FMVerifier v )
        throws Exception
    {
        if ( !main.exists() )
        {
            throw new FileNotFoundException( PathUtil.path( main ) );
        }

        Process p = null;
        try
        {
            p = Runtime.getRuntime().exec( new String[] { path( flashplayer ), path( main ) } );
            final Process tp = p;

            Thread t = new Thread( new Runnable()
            {

                public void run()
                {
                    try
                    {
                        tp.waitFor();
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
            } );

            t.start();

            t.join( 10000 );

            int exitValue = p.exitValue();
            if ( OSUtils.isWindows() )
            {
                MatcherAssert.assertThat( exitValue, CoreMatchers.equalTo( expectedExitCode ) );
            }
        }
        finally
        {
            if ( p != null )
                p.destroy();
        }
    }

    protected String siteGoal()
    {
        return "org.apache.maven.plugins:maven-site-plugin:" + getProperty( "maven-site-plugin.version" ) + ":site";
    }

}
