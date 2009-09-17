/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

public class ApplicationDependencySorter
    extends FlexDependencySorter
{
    public enum StaticRSLScope
    {
        DEFAULT, MERGED, INTERNAL, EXTERNAL
    }

    private String defaultScope;

    private List<Artifact> themes = new ArrayList<Artifact>();

    private List<Artifact> rslLibraries = new ArrayList<Artifact>();

    private List<Artifact> cachingLibraries = new ArrayList<Artifact>();

    private List<Artifact> rslAndCachingArtifacts = new ArrayList<Artifact>();

    public File[] getRSLLibraries()
    {
        return toArray( rslLibraries );
    }

    public File[] getCachingLibraries()
    {
        return toArray( cachingLibraries );
    }

    public List<Artifact> getRSLAndCachingArtifacts()
    {
        return rslAndCachingArtifacts;
    }

    public File[] getThemes()
    {
        return toArray( themes );
    }

    public void sort( MavenProject project, String defaultScope, StaticRSLScope rslScope )
        throws MojoExecutionException
    {
        this.defaultScope = defaultScope;
        super.sort( project );
        sortRSLs( rslScope );
    }

    private void sortRSLs( StaticRSLScope rslScope )
    {
        if ( rslScope == StaticRSLScope.DEFAULT || rslAndCachingArtifacts.isEmpty() )
        {
            return;
        }

        List<Artifact> desiredList = getDesiredListForStaticRSL( rslScope );
        desiredList.addAll( rslLibraries );
        desiredList.addAll( cachingLibraries );

        rslLibraries.clear();
        cachingLibraries.clear();
        rslAndCachingArtifacts.clear();
    }

    private List<Artifact> getDesiredListForStaticRSL( StaticRSLScope rslScope )
    {
        switch ( rslScope )
        {
            case MERGED:
                return mergedArtifacts;
            case INTERNAL:
                return internalArtifacts;
            case EXTERNAL:
                return externalArtifacts;
            default:
                throw new IllegalArgumentException( "unsupported static RSL scope" );
        }
    }

    @Override
    protected boolean sortSWCArtifact( Artifact artifact )
        throws MojoExecutionException
    {
        if ( !super.sortSWCArtifact( artifact ) )
        {
            final String scope = artifact.getScope();
            if ( FlexScopes.RSL.equals( scope ) )
            {
                rslLibraries.add( artifact );
                rslAndCachingArtifacts.add( artifact );
            }
            else if ( FlexScopes.CACHING.equals( scope ) )
            {
                cachingLibraries.add( artifact );
                rslAndCachingArtifacts.add( artifact );
            }
            else if ( FlexScopes.THEME.equals( scope ) )
            {
                themes.add( artifact );
            }
            else
            {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    protected void addToDefaultScope( Artifact artifact )
        throws MojoExecutionException
    {
        mergedArtifacts.add( artifact );
    }

}
