package org.sonatype.flexmojos.plugin.compiler;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionContaining.hasItems;
import static org.hamcrest.text.StringContains.containsString;
import static org.mockito.Mockito.mock;
import static org.sonatype.flexmojos.matcher.file.FileMatcher.withAbsolutePath;
import static org.sonatype.flexmojos.plugin.AbstractMavenMojo.FRAMEWORK_GROUP_ID;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.hamcrest.MatcherAssert;
import org.sonatype.flexmojos.matcher.collection.CollectionsMatcher;
import org.sonatype.flexmojos.plugin.test.TestCompilerMojo;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DependencyFilteringTest
{

    private static PlexusContainer plexus;

    private Set<Artifact> flexArtifacts;

    private static RepositorySystem repositorySystem;

    private LinkedHashSet<Artifact> airArtifacts;

    @BeforeClass
    public static void initPlexus()
        throws Exception
    {
        plexus = new DefaultPlexusContainer();
        repositorySystem = plexus.lookup( RepositorySystem.class );
    }

    @AfterClass
    public static void killPlexus()
    {

        if ( plexus != null )
        {
            try
            {
                plexus.release( repositorySystem );
            }
            catch ( ComponentLifecycleException e )
            {
                // not relevant
            }
            plexus.dispose();
        }

        plexus = null;
        repositorySystem = null;
    }

    @BeforeMethod
    public void initArtifacts()
        throws Exception
    {
        flexArtifacts = new LinkedHashSet<Artifact>();
        flexArtifacts.add( createArtifact( "d", "framework-external", "1.0", "external", "swc", null ) );
        flexArtifacts.add( createArtifact( "d", "rpc-external", "1.0", "external", "swc", null ) );
        flexArtifacts.add( createArtifact( "d", "framework-internal", "1.0", "internal", "swc", null ) );
        flexArtifacts.add( createArtifact( "d", "rpc-internal", "1.0", "internal", "swc", null ) );
        flexArtifacts.add( createArtifact( "d", "framework-compile", "1.0", "compile", "swc", null ) );
        flexArtifacts.add( createArtifact( "d", "rpc-compile", "1.0", null, "swc", null ) );
        flexArtifacts.add( createArtifact( "d", "framework-merged", "1.0", "merged", "swc", null ) );
        flexArtifacts.add( createArtifact( "d", "rpc-merged", "1.0", "merged", "swc", null ) );
        flexArtifacts.add( createArtifact( "d", "framework-rb", "1.0", "internal", "rb.swc", null ) );
        flexArtifacts.add( createArtifact( "d", "rpc-rb", "1.0", null, "rb.swc", null ) );
        flexArtifacts.add( createArtifact( "com.adobe.flexunit", "flexunit", "4.0", "test", "swc", null ) );
        flexArtifacts.add( createArtifact( "d", "rpc-test", "1.0", "test", "swc", null ) );

        airArtifacts = new LinkedHashSet<Artifact>( flexArtifacts );

        flexArtifacts.add( createArtifact( FRAMEWORK_GROUP_ID, "playerglobal", "1.0", "provided", "swc", "10" ) );
        airArtifacts.add( createArtifact( FRAMEWORK_GROUP_ID, "airglobal", "1.0", "provided", "swc", null ) );
    }

    private Artifact createArtifact( String groupId, String artifactId, String version, String scope, String type,
                                     String classifier )
    {
        Artifact a = repositorySystem.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );
        a.setScope( scope );
        a.setResolved( true );

        classifier = classifier == null ? "" : "-" + classifier;

        File f = new File( "target/swcs", artifactId + classifier + "-" + version + "." + type );
        f.getParentFile().mkdirs();
        try
        {
            f.createNewFile();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( f.getAbsolutePath(), e );
        }

        a.setFile( f );
        return a;
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void swf()
    {
        MxmlcMojo c = new MxmlcMojo()
        {
            @Override
            public Set<Artifact> getDependencies()
            {
                return flexArtifacts;
            }

            @Override
            protected Artifact resolve( String groupId, String artifactId, String version, String classifier,
                                        String type )
            {
                return createArtifact( groupId, artifactId, version, null, type, classifier );
            }

            @Override
            public File getOutputDirectory()
            {
                return new File( "target/temp" );
            }
        };
        c.setLog( mock( Log.class ) );
        c.toolsLocale = "en_US";

        List<File> deps = Arrays.asList( c.getExternalLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 3 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-external" ) ),//
                                                  withAbsolutePath( containsString( "rpc-external" ) ),//
                                                  withAbsolutePath( containsString( "playerglobal.swc" ) ) ) );

        deps = Arrays.asList( c.getLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 6 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-merged" ) ),//
                                                  withAbsolutePath( containsString( "rpc-merged" ) ),//
                                                  withAbsolutePath( containsString( "framework-rb-en_US" ) ),//
                                                  withAbsolutePath( containsString( "rpc-rb-en_US" ) ),//
                                                  withAbsolutePath( containsString( "framework-compile" ) ),//
                                                  withAbsolutePath( containsString( "rpc-compile" ) ) ) );

        MatcherAssert.assertThat( deps, not( hasItems( withAbsolutePath( containsString( "playerglobal.swc" ) ) ) ) );

        deps = Arrays.asList( c.getIncludeLibraries() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 2 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-internal" ) ),//
                                                  withAbsolutePath( containsString( "rpc-internal" ) ) ) );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void swc()
    {
        CompcMojo c = new CompcMojo()
        {
            @Override
            public Set<Artifact> getDependencies()
            {
                return flexArtifacts;
            }

            @Override
            public File getOutputDirectory()
            {
                return new File( "target/temp" );
            }
        };
        c.setLog( mock( Log.class ) );

        List<File> deps = Arrays.asList( c.getExternalLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 5 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-external" ) ),//
                                                  withAbsolutePath( containsString( "rpc-external" ) ),//
                                                  withAbsolutePath( containsString( "framework-compile" ) ),//
                                                  withAbsolutePath( containsString( "rpc-compile" ) ),//
                                                  withAbsolutePath( containsString( "playerglobal.swc" ) ) ) );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void asdoc()
    {
        AsdocMojo c = new AsdocMojo()
        {
            @Override
            public Set<Artifact> getDependencies()
            {
                return airArtifacts;
            }

            @Override
            protected Artifact resolve( String groupId, String artifactId, String version, String classifier,
                                        String type )
            {
                return createArtifact( groupId, artifactId, version, null, type, classifier );
            }

            @Override
            public File getOutputDirectory()
            {
                return new File( "target/temp" );
            }

            @Override
            public String getProjectType()
            {
                return "air";
            }

            @Override
            public String[] getLocale()
            {
                return new String[] { "en_US" };
            }
        };
        c.setLog( mock( Log.class ) );

        c.toolsLocale = "en_US";

        List<File> deps = Arrays.asList( c.getExternalLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 1 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "airglobal.swc" ) ) ) );

        deps = Arrays.asList( c.getLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 10 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-internal" ) ),//
                                                  withAbsolutePath( containsString( "rpc-internal" ) ),//
                                                  withAbsolutePath( containsString( "framework-external" ) ),//
                                                  withAbsolutePath( containsString( "rpc-external" ) ),//
                                                  withAbsolutePath( containsString( "framework-merged" ) ),//
                                                  withAbsolutePath( containsString( "rpc-merged" ) ),//
                                                  withAbsolutePath( containsString( "framework-rb-en_US" ) ),//
                                                  withAbsolutePath( containsString( "rpc-rb-en_US" ) ),//
                                                  withAbsolutePath( containsString( "framework-compile" ) ),//
                                                  withAbsolutePath( containsString( "rpc-compile" ) ) ) );

        MatcherAssert.assertThat( deps, not( hasItems( withAbsolutePath( containsString( "airglobal.swc" ) ) ) ) );

        MatcherAssert.assertThat( c.getIncludeLibraries(), nullValue() );
        MatcherAssert.assertThat( c.getRuntimeSharedLibraries(), nullValue() );
        MatcherAssert.assertThat( c.getRuntimeSharedLibraryPath(), nullValue() );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void test()
    {
        TestCompilerMojo c = new TestCompilerMojo()
        {
            @Override
            public Set<Artifact> getDependencies()
            {
                return flexArtifacts;
            }

            @Override
            protected Artifact resolve( String groupId, String artifactId, String version, String classifier,
                                        String type )
            {
                return createArtifact( groupId, artifactId, version, null, type, classifier );
            }

            @Override
            public File getOutputDirectory()
            {
                return new File( "target/temp" );
            }
        };
        c.setLog( mock( Log.class ) );
        c.toolsLocale = "en_US";

        List<File> deps = Arrays.asList( c.getExternalLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 1 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "playerglobal.swc" ) ) ) );

        deps = Arrays.asList( c.getIncludeLibraries() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 6 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-internal" ) ),//
                                                  withAbsolutePath( containsString( "rpc-internal" ) ),//
                                                  withAbsolutePath( containsString( "flexunit" ) ),//
                                                  withAbsolutePath( containsString( "flexmojos-unittest-flexunit4" ) ),//
                                                  withAbsolutePath( containsString( "flexmojos-unittest-support" ) ),//
                                                  withAbsolutePath( containsString( "rpc-test" ) ) ) );

        deps = Arrays.asList( c.getLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 8 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-external" ) ),//
                                                  withAbsolutePath( containsString( "rpc-external" ) ),//
                                                  withAbsolutePath( containsString( "framework-merged" ) ),//
                                                  withAbsolutePath( containsString( "rpc-merged" ) ),//
                                                  withAbsolutePath( containsString( "framework-rb-en_US" ) ),//
                                                  withAbsolutePath( containsString( "rpc-rb-en_US" ) ),//
                                                  withAbsolutePath( containsString( "framework-compile" ) ),//
                                                  withAbsolutePath( containsString( "rpc-compile" ) ) ) );
    }

}
