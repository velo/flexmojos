package org.sonatype.flexmojos.common;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public class TestApplicationDependencySorter
    extends ApplicationDependencySorter
{
    @Override
    public void sort( MavenProject project )
        throws MojoExecutionException
    {
        super.sort( project, FlexScopes.MERGED, StaticRSLScope.INTERNAL );

        internalLibraries.addAll( externalLibraries );
        internalLibraries.addAll( testLibraries );

        externalLibraries.clear();
        testLibraries.clear();
    }
}
