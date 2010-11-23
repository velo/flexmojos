package org.sonatype.flexmojos.plugin.compiler;

import static org.testng.Assert.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.sonatype.flexmojos.compiler.FlexCompiler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ResourceBundleChainingTest
{

    private static PlexusContainer plexus;

    private static RepositorySystem repositorySystem;

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

    private Artifact createArtifact( String groupId, String artifactId, String version, String scope, String type,
                                     String classifier )
    {
        Artifact a = repositorySystem.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );
        a.setScope( scope );
        a.setResolved( true );

        classifier = classifier == null ? "" : "-" + classifier;

        File f = new File( "target/test-classes/swcs", artifactId + classifier + "-" + version + "." + type );
        if ( !f.exists() )
        {
            f.getParentFile().mkdirs();
            try
            {
                f.createNewFile();
            }
            catch ( IOException e )
            {
                throw new RuntimeException( f.getAbsolutePath(), e );
            }
        }

        a.setFile( f );
        return a;
    }

    @Test( enabled = false )
    public void adaptRbSwc()
        throws Exception
    {
        // FIXME
        final Log log = new SystemStreamLog();
        MxmlcMojo c = new MxmlcMojo()
        {
            @Override
            public File getOutputDirectory()
            {
                File f = new File( "target/temp" );
                f.mkdirs();
                return f;
            }

            @Override
            public Set<Artifact> getDependencies()
            {
                Set<Artifact> set = new LinkedHashSet<Artifact>();
                set.add( createArtifact( FRAMEWORK_GROUP_ID, "playerglobal", "4", "provided", "swc", "10" ) );
                set.add( createArtifact( FRAMEWORK_GROUP_ID, "framework", "4", "compile", "swc", null ) );
                return set;
            }

            @Override
            public Log getLog()
            {
                return log;
            }
        };
        c.compiler = plexus.lookup( FlexCompiler.class );
        c.setArchiverManager( plexus.lookup( ArchiverManager.class ) );
        Artifact base = createArtifact( "some.group", "artifact", "1.0", "compile", "rb.swc", "en_US" );
        Artifact desired = c.adaptResourceBundle( base, "en_GB" );
        assertTrue( desired.isResolved() );
    }

}
