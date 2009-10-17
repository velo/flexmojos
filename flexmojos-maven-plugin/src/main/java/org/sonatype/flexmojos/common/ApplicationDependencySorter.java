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
