package org.sonatype.flexmojos.common;

import static org.sonatype.flexmojos.common.AbstractMavenFlexCompilerConfiguration.FRAMEWORK_GROUP_ID;
import static org.testng.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.text.StringContains;
import org.sonatype.flexmojos.common.matcher.CollectionsMatcher;
import org.sonatype.flexmojos.common.matcher.FileMatcher;
import org.sonatype.flexmojos.compiler.CompcMojo;
import org.sonatype.flexmojos.compiler.MxmlcMojo;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

public class DependencyFilteringTest
{

    private static PlexusContainer plexus;

    private Set<Artifact> artifacts;

    private RepositorySystem repositorySystem;

    @BeforeClass
    public static void initPlexus()
        throws Exception
    {
        plexus = new DefaultPlexusContainer();
    }

    @AfterClass
    public static void killPlexus()
    {
        if ( plexus != null )
        {
            plexus.dispose();
        }
    }

    @BeforeMethod
    public void initArtifacts()
        throws Exception
    {
        this.repositorySystem = plexus.lookup( RepositorySystem.class );

        artifacts = new LinkedHashSet<Artifact>();
        Artifact artifact =
            repositorySystem.createArtifactWithClassifier( FRAMEWORK_GROUP_ID, "playerglobal", "1.0", "swc", "10" );
        artifact.setResolved( true );
        artifact.setFile( new File( artifact.getArtifactId() + "." + artifact.getType() ) );

        artifacts.add( artifact );
        artifacts.add( createArtifact( "d", "framework-external", "1.0", "external", "swc" ) );
        artifacts.add( createArtifact( "d", "rpc-external", "1.0", "external", "swc" ) );
        artifacts.add( createArtifact( "d", "framework-internal", "1.0", "internal", "swc" ) );
        artifacts.add( createArtifact( "d", "rpc-internal", "1.0", "internal", "swc" ) );
        artifacts.add( createArtifact( "d", "framework-compile", "1.0", "compile", "swc" ) );
        artifacts.add( createArtifact( "d", "rpc-compile", "1.0", "compile", "swc" ) );
        artifacts.add( createArtifact( "d", "framework-merged", "1.0", "merged", "swc" ) );
        artifacts.add( createArtifact( "d", "rpc-merged", "1.0", "merged", "swc" ) );
    }

    private Artifact createArtifact( String groupId, String artifactId, String version, String scope, String type )
    {
        Artifact a = repositorySystem.createArtifact( groupId, artifactId, version, scope, type );
        a.setResolved( true );
        a.setFile( new File( artifactId + "." + type ) );
        return a;
    }

    @Test
    public void swf()
    {
        CompcMojo c = new CompcMojo()
        {
            @Override
            public String getProjectType()
            {
                return "swf";
            }

            @Override
            public Set<Artifact> getDependencies()
            {
                return artifacts;
            }
        };

        List<File> deps = Arrays.asList( c.getExternalLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 3 ) );
        MatcherAssert.assertThat(
                                  deps,
                                  IsCollectionContaining.hasItems(
                                                                   FileMatcher.withAbsolutePath( StringContains.containsString( "framework-external" ) ),//
                                                                   FileMatcher.withAbsolutePath( StringContains.containsString( "rpc-external" ) ),//
                                                                   FileMatcher.withAbsolutePath( StringContains.containsString( "playerglobal" ) ) ) );
    }

    @Test
    public void swc()
    {
        MxmlcMojo c = new MxmlcMojo()
        {
            @Override
            public String getProjectType()
            {
                return "swc";
            }

            @Override
            public Set<Artifact> getDependencies()
            {
                return artifacts;
            }
        };

        List<File> deps = Arrays.asList( c.getExternalLibraryPath() );
        MatcherAssert.assertThat( deps, CollectionsMatcher.isSize( 5 ) );
        MatcherAssert.assertThat(
                                  deps,
                                  IsCollectionContaining.hasItems(
                                                                   FileMatcher.withAbsolutePath( StringContains.containsString( "framework-external" ) ),//
                                                                   FileMatcher.withAbsolutePath( StringContains.containsString( "rpc-external" ) ),//
                                                                   FileMatcher.withAbsolutePath( StringContains.containsString( "framework-compile" ) ),//
                                                                   FileMatcher.withAbsolutePath( StringContains.containsString( "rpc-compile" ) ),//
                                                                   FileMatcher.withAbsolutePath( StringContains.containsString( "playerglobal" ) ) ) );

    }

    @Test
    public void testGetLibraryPath()
    {
        fail( "Not yet implemented" );
    }

}
