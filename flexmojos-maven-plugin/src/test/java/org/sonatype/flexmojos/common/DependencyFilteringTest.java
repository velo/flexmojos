package org.sonatype.flexmojos.common;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsCollectionContaining.hasItems;
import static org.hamcrest.text.StringContains.containsString;
import static org.sonatype.flexmojos.common.AbstractMavenFlexCompilerConfiguration.FRAMEWORK_GROUP_ID;
import static org.sonatype.flexmojos.common.matcher.FileMatcher.withAbsolutePath;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.hamcrest.MatcherAssert;
import org.sonatype.flexmojos.common.matcher.CollectionsMatcher;
import org.sonatype.flexmojos.compiler.AsdocMojo;
import org.sonatype.flexmojos.compiler.CompcMojo;
import org.sonatype.flexmojos.compiler.MxmlcMojo;
import org.testng.Assert;
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

        a.setFile( new File( artifactId + classifier + "-" + version + "." + type ) );
        return a;
    }

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
            public String getToolsLocale()
            {
                return "??";
            }

            @Override
            protected Artifact resolve( String groupId, String artifactId, String version, String classifier,
                                        String type )
            {
                return createArtifact( groupId, artifactId, version, null, type, classifier );
            }
        };
        c.outputDirectory = new File("target/temp");

        validate( c, "playerglobal.swc" );
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
        };
        c.outputDirectory = new File("target/temp");

        List<File> deps = Arrays.asList( c.getExternalLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 5 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-external" ) ),//
                                                  withAbsolutePath( containsString( "rpc-external" ) ),//
                                                  withAbsolutePath( containsString( "framework-compile" ) ),//
                                                  withAbsolutePath( containsString( "rpc-compile" ) ),//
                                                  withAbsolutePath( containsString( "playerglobal.swc" ) ) ) );

    }

    @Test
    public void air()
    {
        AsdocMojo c = new AsdocMojo()
        {
            @Override
            public Set<Artifact> getDependencies()
            {
                return airArtifacts;
            }

            @Override
            public String getToolsLocale()
            {
                return "??";
            }

            @Override
            protected Artifact resolve( String groupId, String artifactId, String version, String classifier,
                                        String type )
            {
                return createArtifact( groupId, artifactId, version, null, type, classifier );
            }

        };
        c.outputDirectory = new File("target/temp");
        c.packaging = "air";

        validate( c, "airglobal.swc" );
    }

    @Test
    public void test()
    {
        Assert.fail();
    }

    @SuppressWarnings( "unchecked" )
    private void validate( AbstractMavenFlexCompilerConfiguration c, String globalDep )
    {
        List<File> deps = Arrays.asList( c.getExternalLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 3 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-external" ) ),//
                                                  withAbsolutePath( containsString( "rpc-external" ) ),//
                                                  withAbsolutePath( containsString( globalDep ) ) ) );

        deps = Arrays.asList( c.getLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 6 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-merged" ) ),//
                                                  withAbsolutePath( containsString( "rpc-merged" ) ),//
                                                  withAbsolutePath( containsString( "framework-rb-??" ) ),//
                                                  withAbsolutePath( containsString( "rpc-rb-??" ) ),//
                                                  withAbsolutePath( containsString( "framework-compile" ) ),//
                                                  withAbsolutePath( containsString( "rpc-compile" ) ) ) );

        MatcherAssert.assertThat( deps, not( hasItems( withAbsolutePath( containsString( globalDep ) ) ) ) );

        deps = Arrays.asList( c.getIncludeLibraries() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 2 ) );
        MatcherAssert.assertThat( deps, hasItems( withAbsolutePath( containsString( "framework-internal" ) ),//
                                                  withAbsolutePath( containsString( "rpc-internal" ) ) ) );

    }
}
