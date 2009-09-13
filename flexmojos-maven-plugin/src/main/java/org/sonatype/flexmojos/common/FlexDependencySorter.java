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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private List<File> linkReports = new ArrayList<File>();

    protected List<File> externalLibraries = new ArrayList<File>();

    protected List<File> internalLibraries = new ArrayList<File>();

    protected List<File> mergedLibraries = new ArrayList<File>();

    protected List<File> testLibraries = new ArrayList<File>();

    private List<Artifact> resourceBundleArtifacts = new ArrayList<Artifact>();

    private List<File> globalLibraries = new ArrayList<File>();

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
        return toArray( globalLibraries );
    }

    public File[] getLinkReports()
    {
        return toArray( linkReports );
    }

    public File[] getExternalLibraries()
    {
        return toArray( externalLibraries );
    }

    public File[] getInternalLibraries()
    {
        return toArray( internalLibraries );
    }

    public File[] getMergedLibraries()
    {
        return toArray( mergedLibraries );
    }

    public File[] getTestLibraries()
    {
        return toArray( testLibraries );
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
            globalLibraries.add( copyGlobalArtifactWorkaround() );
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
            linkReports.add( artifact.getFile() );
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
            externalLibraries.add( artifact.getFile() );
        }
        else if ( scope.equals( FlexScopes.INTERNAL ) )
        {
            internalLibraries.add( artifact.getFile() );
        }
        else if ( scope.equals( FlexScopes.MERGED ) )
        {
            mergedLibraries.add( artifact.getFile() );
        }
        else if ( scope.equals( Artifact.SCOPE_TEST ) )
        {
            testLibraries.add( artifact.getFile() );
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
        externalLibraries.add( artifact.getFile() );
    }

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
        if ( globalArtifactsDepthMap.containsKey( mapKey ))
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

    /**
     * @todo autoclean on version change (user must be able just change version in POM, without additional mvn clean)
     * stupid adobe compiler determine global artifact by name
     * (maven artifact contains version info in filename)
     */
    private File copyGlobalArtifactWorkaround()
        throws MojoExecutionException
    {
        File dest = new File( project.getBuild().getOutputDirectory(),
                              globalArtifact.getArtifactId() + "." + FlexExtension.SWC );
        if ( !dest.exists() )
        {
            try
            {
                FileUtils.copyFile( globalArtifact.getFile(), dest );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }

        return dest;
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
        else if ( fdkVersion == null && MavenExtension.POM.equals( artifact.getType() ) )
        {
            final String artifactId = artifact.getArtifactId();
            for ( String fdkId : FDK_VERSION_ARTIFACTS_IDS )
            {
                if ( artifactId.equals( fdkId ) )
                {
                    fdkVersion = artifact.getVersion();
                }
            }
        }
    }

    /**
     * Vladimir Krivosheev: temp, we remove it after "flexmojos to stop using OEM API and use a lower level API"
     */
    private Map<List<File>, File[]> listArrayMap = new HashMap<List<File>, File[]>( 8 );

    protected File[] toArray( List<File> list )
    {
        if ( listArrayMap.containsKey( list ) )
        {
            return listArrayMap.get( list );
        }
        else
        {
            File[] array = list.toArray( new File[list.size()] );
            listArrayMap.put( list, array );
            return array;
        }
    }
}
