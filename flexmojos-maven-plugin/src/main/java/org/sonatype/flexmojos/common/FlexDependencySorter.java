/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * @since 3.4
 */
public class FlexDependencySorter
{
    private static final String DEPENDENCY_TRAIL_SWC = ":" + FlexExtension.SWC + ":";

    private static final String[] FDK_VERSION_ARTIFACTS_IDS =
        new String[] { "flex-framework", "air-framework", "framework", "airframework" };

    private static final String PLAYER_GLOBAL = "playerglobal";

    private static final String AIR_GLOBAL = "airglobal";

    private MavenProject project;

    private String fdkVersion;

    private File fdkConfigFile;

    private boolean isAIR;

    private List<Artifact> linkReports = new ArrayList<Artifact>();

    protected List<Artifact> externalArtifacts = new ArrayList<Artifact>();

    protected List<Artifact> internalArtifacts = new ArrayList<Artifact>();

    protected List<Artifact> mergedArtifacts = new ArrayList<Artifact>();

    protected List<Artifact> testArtifacts = new ArrayList<Artifact>();

    private List<Artifact> resourceBundleArtifacts = new ArrayList<Artifact>();

    private Artifact globalArtifact;

    private SortedMap<Integer, List<Artifact>> globalArtifactsDepthMap = new TreeMap<Integer, List<Artifact>>();

    public File getFDKConfigFile()
    {
        return fdkConfigFile;
    }

    public String getFDKVersion()
    {
        return fdkVersion;
    }

    public boolean isAIR()
    {
        return isAIR;
    }

    public Artifact getGlobalArtifact()
    {
        return globalArtifact;
    }

    public File[] getGlobalLibraries()
    {
        return new File[] { globalArtifact.getFile() };
    }

    public File[] getLinkReports()
    {
        return toArray( linkReports );
    }

    public File[] getExternalLibraries()
    {
        return toArray( externalArtifacts );
    }

    public File[] getInternalLibraries()
    {
        return toArray( internalArtifacts );
    }

    public File[] getMergedLibraries()
    {
        return toArray( mergedArtifacts );
    }

    public File[] getTestLibraries()
    {
        return toArray( testArtifacts );
    }

    public List<Artifact> getResourceBundleArtifacts()
    {
        return resourceBundleArtifacts;
    }

    @SuppressWarnings( "unchecked" )
    public void sort( MavenProject project )
        throws MojoExecutionException
    {
        this.project = project;

        for ( Artifact artifact : (Set<Artifact>) project.getArtifacts() )
        {
            sortArtifact( artifact );
        }

        if ( globalArtifactsDepthMap.isEmpty() )
        {
            throw new MojoExecutionException( "Player/AIR Global dependency not found." );
        }
        else
        {
            List<Artifact> artifacts = globalArtifactsDepthMap.get( globalArtifactsDepthMap.firstKey() );
            if ( artifacts.size() != 1 )
            {
                throw new MojoExecutionException( "Multiple Player/AIR Global dependencies.\n" + artifacts );
            }

            globalArtifact = artifacts.get( 0 );
            copyGlobalArtifactWorkaround();
            isAIR = AIR_GLOBAL.equals( globalArtifact.getArtifactId() );

            globalArtifactsDepthMap = null;
        }
    }

    /**
     * @return true if artifact sorted, false otherwise
     */
    protected boolean sortArtifact( Artifact artifact )
        throws MojoExecutionException
    {
        if ( FlexClassifier.LINK_REPORT.equals( artifact.getClassifier() ) )
        {
            linkReports.add( artifact );
        }
        else if ( FlexExtension.SWC.equals( artifact.getType() ) )
        {
            if ( PLAYER_GLOBAL.equals( artifact.getArtifactId() ) || AIR_GLOBAL.equals( artifact.getArtifactId() ) )
            {
                sortGlobalArtifact( artifact );
            }
            else
            {
                return sortSWCArtifact( artifact );
            }
        }
        else if ( FlexExtension.RB_SWC.equals( artifact.getType() ) )
        {
            resourceBundleArtifacts.add( artifact );
        }
        else if ( fdkConfigFile == null && artifact.getGroupId().equals( "com.adobe.flex.framework" ) )
        {
            checkFDKConfigAndVersion( artifact );
        }
        else
        {
            return false;
        }

        return true;
    }

    protected boolean sortSWCArtifact( Artifact artifact )
        throws MojoExecutionException
    {
        final String scope = artifact.getScope();
        if ( scope.equals( Artifact.SCOPE_COMPILE ) )
        {
            addToDefaultScope( artifact );
        }
        else if ( scope.equals( FlexScopes.EXTERNAL ) )
        {
            externalArtifacts.add( artifact );
        }
        else if ( scope.equals( FlexScopes.INTERNAL ) )
        {
            internalArtifacts.add( artifact );
        }
        else if ( scope.equals( FlexScopes.MERGED ) )
        {
            mergedArtifacts.add( artifact );
        }
        else if ( scope.equals( Artifact.SCOPE_TEST ) )
        {
            testArtifacts.add( artifact );
        }
        else
        {
            return false;
        }

        return true;
    }

    protected void addToDefaultScope( Artifact artifact )
        throws MojoExecutionException
    {
        externalArtifacts.add( artifact );
    }

    @SuppressWarnings( "unchecked" )
    private void sortGlobalArtifact( Artifact artifact )
        throws MojoExecutionException
    {
        List<String> dependencyTrail = artifact.getDependencyTrail();
        // i = 1, because first item is project artifact; size - 1, because last item is current artifact
        for ( int i = 1, n = dependencyTrail.size() - 1; i < n; i++ )
        {
            if ( dependencyTrail.get( i ).lastIndexOf( DEPENDENCY_TRAIL_SWC ) != -1 )
            {
                return;
            }
        }

        final Integer mapKey = dependencyTrail.size();
        List<Artifact> artifacts;
        if ( globalArtifactsDepthMap.containsKey( mapKey ) )
        {
            artifacts = globalArtifactsDepthMap.get( mapKey );
        }
        else
        {
            artifacts = new ArrayList<Artifact>();
            globalArtifactsDepthMap.put( mapKey, artifacts );
        }
        artifacts.add( artifact );
    }

    private void copyGlobalArtifactWorkaround()
        throws MojoExecutionException
    {
        File dest =
            new File( project.getBuild().getOutputDirectory(), globalArtifact.getArtifactId() + "." + FlexExtension.SWC );

        // must overwrite if the dependency changed
        try
        {
            File globalDepFile = globalArtifact.getFile();
            if ( !FileUtils.contentEquals( globalDepFile, dest ) )
            {
                FileUtils.copyFile( globalDepFile, dest );
                dest.setLastModified( globalDepFile.lastModified() );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        globalArtifact.setFile( dest );
    }

    private void checkFDKConfigAndVersion( Artifact artifact )
    {
        if ( FlexClassifier.CONFIGS.equals( artifact.getClassifier() ) )
        {
            fdkConfigFile = artifact.getFile();
            if ( fdkVersion == null )
            {
                fdkVersion = artifact.getVersion();
            }
        }
        else if ( fdkVersion == null && FlexExtension.POM.equals( artifact.getType() ) )
        {
            final String artifactId = artifact.getArtifactId();
            for ( String fdkId : FDK_VERSION_ARTIFACTS_IDS )
            {
                if ( artifactId.equals( fdkId ) )
                {
                    fdkVersion = artifact.getVersion();
                    break;
                }
            }
        }
    }

    protected File[] toArray( List<Artifact> artifacts )
    {
        List<File> files = new ArrayList<File>();
        for ( Artifact artifact : artifacts )
        {
            files.add( artifact.getFile() );
        }

        return files.toArray( new File[files.size()] );
    }

    public void addArtifact( Artifact artifact )
        throws MojoExecutionException
    {
        sortSWCArtifact( artifact );
    }

    public List<Artifact> getTestArtifacts()
    {
        return testArtifacts;
    }

}
