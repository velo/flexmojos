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
package net.flexmojos.oss.tests;

import static net.flexmojos.oss.util.PathUtil.path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.maven.it.VerificationException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelProcessor;
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
import net.flexmojos.oss.test.FMVerifier;
import net.flexmojos.oss.test.report.TestCaseReport;
import net.flexmojos.oss.util.OSUtils;
import net.flexmojos.oss.util.PathUtil;
import org.jetbrains.annotations.Nullable;
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

    private static File repo;

    protected static PlexusContainer container;

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
        repo = new File( getProperty( "fake-repo" ));

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
        addCobertura( new File( repo, "net/flexmojos/oss/flexmojos-parent/" + getFlexmojosVersion()
            + "/flexmojos-parent-" + getFlexmojosVersion() + ".pom" ) );
        addCobertura( new File( repo, "net/flexmojos/oss/flexmojos-maven-plugin/" + getFlexmojosVersion()
            + "/flexmojos-maven-plugin-" + getFlexmojosVersion() + ".pom" ) );
    }

    private static void addCobertura( File fmParentPom )
        throws ComponentLookupException, IOException
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
        return getProjectCustom( projectName, null, null, filesToInterpolate );
    }

    protected File getProjectWithForcedSdk( String projectName, String sdkVersion, String... filesToInterpolate )
            throws IOException
    {
        return getProjectCustom( projectName, null, sdkVersion, filesToInterpolate );
    }

    protected File getProjectCustom( String projectName, String output, String... filesToInterpolate )
        throws IOException
    {
        return getProjectCustom( projectName, output, null, filesToInterpolate );
    }

    protected File getProjectCustom( String projectName, @Nullable String output, @Nullable String sdkVersion, String... filesToInterpolate )
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
                output = projectName + "_" + getTestName() + ( ( sdkVersion != null ) ? "-" + sdkVersion : "" );
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
                pomContent = pomContent.replace( "%{flex.groupId}", getFlexGroupId() );
                pomContent = pomContent.replace( "%{flex.version}", getFlexVersion() );
                pomContent = pomContent.replace( "%{air.version}", getAirVersion() );
                pomContent = pomContent.replace( "%{player.version}", getPlayerVersion() );
                if( sdkVersion != null)
                {
                    pomContent = pomContent.replace( "${fdkVersion}", sdkVersion );
                    pomContent = pomContent.replace( "${fdkGroupId}", getFlexGroupId(sdkVersion) );
                }
                FileUtils.fileWrite( path( pom ), pomContent );
            }

            return destDir;
        }
        finally
        {
            copyProjectLock.writeLock().unlock();
        }
    }

    protected static String getFlexGroupId()
    {
        return getProperty( "flex-groupId" );
    }

    protected static String getFlexGroupId(String version)
    {
        // TODO: This should be solved differently ...
        /*if("4.8.0.1359417".equals(version)) {
            return "org.apache.flex";
        } else {*/
            return "com.adobe.flex";
        //}
    }

    protected static String getFlexCompilerGroupId()
    {
        return getFlexGroupId() + ".compiler";
    }

    protected static String getFlexFrameworkGroupId()
    {
        return getFlexGroupId() + ".framework";
    }

    protected static String getFlexVersion()
    {
        return getProperty( "flex-version" );
    }

    protected static String getAirVersion()
    {
        return getProperty( "air-version" );
    }

    /**
     * @param groupId artifactId of the artifact you want to get the version of.
     * @param artifactId artifactId of the artifact you want to get the version of.
     * @return version of the given artifact as defined in the corresponding framework-pom.
     */
    protected static String getArtifactVersion( String groupId, String artifactId )
    {
        return getFrameworkVersions().get(groupId + ":" + artifactId);
    }

    private static Map<String, String> frameworkVersions;

    protected static Map<String, String> getFrameworkVersions()
    {
        if(frameworkVersions == null) {
            frameworkVersions = new HashMap<String, String>();

            final String flexGroupIp = getFlexGroupId();
            final String flexVersion = getFlexVersion();

            final File frameworkVersionPom = new File(repo, flexGroupIp.replace(".", "/") + "/framework/" +
                    flexVersion + "/framework-" + flexVersion + ".pom");

            // Check that the file exists.
            AssertJUnit.assertTrue("Couldn't find the framework versions pom at " + frameworkVersionPom.getAbsolutePath(),
                    frameworkVersionPom.exists());

            try {
                // Parse the document.
                final Xpp3Dom document = Xpp3DomBuilder.build( new FileReader( frameworkVersionPom ) );

                // Get all dependency elements.
                final Xpp3Dom[] dependencies = document.getChild("dependencyManagement").getChild(
                        "dependencies").getChildren("dependency");

                // Add them to the index.
                for(final Xpp3Dom dependency : dependencies) {
                    final String groupId = dependency.getChild("groupId").getValue();
                    final String artifactId = dependency.getChild("artifactId").getValue();
                    final String version = dependency.getChild("version").getValue();
                    frameworkVersions.put(groupId + ":" + artifactId, version);
                }
            } catch(Exception e) {
                Assert.fail( "Unable to parse \n" + frameworkVersionPom, e );
            }
        }

        return frameworkVersions;
    }

    protected static String getPlayerVersion()
    {
        return getProperty( "player-version" );
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

        final TestCaseReport report;
        report = new TestCaseReport( Xpp3DomBuilder.build( new FileReader( reportFile ) ) );
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
            p = Runtime.getRuntime().exec( new String[] { "flashplayer", path( main ) } );
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
                        // Ingore.
                    }
                }
            } );

            t.start();

            t.join( 15000 );

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
