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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.hamcrest.MatcherAssert;
import org.sonatype.flexmojos.compiler.FlexCompiler;
import org.sonatype.flexmojos.matcher.collection.CollectionsMatcher;
import org.sonatype.flexmojos.plugin.test.TestCompilerMojo;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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

    @Test
    public void adaptRbSwc()
        throws Exception
    {
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
                set.add( createArtifact( FRAMEWORK_GROUP_ID, "playerglobal", "4", "provided", "swc", "10.1" ) );
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
        Artifact desired = createArtifact( "some.group", "artifact", "1.0", "compile", "rb.swc", "en_GB" );
        c.adaptResourceBundle( base, desired );

    }

}
