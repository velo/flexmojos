/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.plugin.war;

import static org.sonatype.flexmojos.util.PathUtil.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.flexmojos.plugin.AbstractMavenMojo;
import org.sonatype.flexmojos.plugin.common.FlexExtension;
import org.sonatype.flexmojos.plugin.common.FlexScopes;
import org.sonatype.flexmojos.plugin.utilities.CompileConfigurationLoader;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;

/**
 * Goal to copy flex artifacts into war projects.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.0
 * @goal copy-flex-resources
 * @phase process-resources
 * @requiresDependencyResolution
 */
public class CopyMojo
    extends AbstractMavenMojo
    implements FlexScopes, FlexExtension
{

    /**
     * @parameter default-value="true"
     */
    private boolean copyRSL;

    /**
     * @parameter default-value="true"
     */
    private boolean copyRuntimeLocales;

    /**
     * @component
     */
    private ProjectBuilder projectBuilder;

    /**
     * Skip mojo execution
     * 
     * @parameter default-value="false" expression="${flexmojos.copy.skip}"
     */
    private boolean skip;

    /**
     * When true will strip artifact and version information from the built MXML module artifact.
     * 
     * @parameter default-value="false"
     */
    private boolean stripModuleArtifactInfo;

    /**
     * Strip artifact version during copy
     * 
     * @parameter default-value="false"
     */
    private boolean stripVersion;

    /**
     * Use final name if/when available
     * 
     * @parameter default-value="true"
     */
    private boolean useFinalName;

    /**
     * The directory where the webapp is built.
     * 
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private File webappDirectory;

    private void copy( File sourceFile, File destFile )
        throws MojoExecutionException
    {
        try
        {
            FileUtils.copyFile( sourceFile, destFile );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy " + sourceFile, e );
        }
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info( "Skipping copy-mojo execution" );
            return;
        }

        String packaging = project.getPackaging();

        if ( !"war".equals( packaging ) )
        {
            getLog().warn( "'copy-flex-resources' was intended to run on war project" );
        }

        webappDirectory.mkdirs();

        List<Artifact> swfDependencies = getSwfArtifacts();

        for ( Artifact artifact : swfDependencies )
        {
            File sourceFile = artifact.getFile();
            File destFile = getDestinationFile( artifact );

            copy( sourceFile, destFile );
            if ( copyRSL || copyRuntimeLocales )
            {
                performSubArtifactsCopy( artifact );
            }
        }

        List<Artifact> airDependencies = getAirArtifacts();

        for ( Artifact artifact : airDependencies )
        {
            File sourceFile = artifact.getFile();
            File destFile = getDestinationFile( artifact );

            copy( sourceFile, destFile );
        }

    }

    private List<Artifact> getAirArtifacts()
    {
        return getArtifacts( AIR, project );
    }

    private List<Artifact> getArtifacts( String type, MavenProject project )
    {
        List<Artifact> swfArtifacts = new ArrayList<Artifact>();
        Set<Artifact> artifacts = project.getArtifacts();
        for ( Artifact artifact : artifacts )
        {
            if ( type.equals( artifact.getType() ) )
            {
                swfArtifacts.add( artifact );
            }
        }
        return swfArtifacts;
    }

    private File getDestinationFile( Artifact artifact )
        throws MojoExecutionException
    {
        boolean isModule = !StringUtils.isEmpty( artifact.getClassifier() );
        MavenProject pomProject = getProject( artifact );
        String fileName;
        if ( isModule )
        {
            if ( !stripModuleArtifactInfo )
            {
                fileName =
                    artifact.getArtifactId() + "-" + artifact.getVersion() + "-" + artifact.getClassifier() + "."
                        + artifact.getType();
            }
            else
            {
                fileName = artifact.getClassifier() + "." + artifact.getType();
            }
        }
        else
        {
            if ( !useFinalName )
            {
                fileName = artifact.getArtifactId() + "-" + artifact.getVersion() + "." + artifact.getType();
            }
            else
            {
                fileName = pomProject.getBuild().getFinalName() + "." + artifact.getType();
            }
        }

        if ( stripVersion && fileName.contains( artifact.getVersion() ) )
        {
            fileName = fileName.replace( "-" + artifact.getVersion(), "" );
        }

        File destFile = new File( webappDirectory, fileName );

        return destFile;
    }

    private MavenProject getProject( Artifact artifact )
        throws MojoExecutionException
    {
        try
        {
            ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
            request.setLocalRepository( localRepository );
            request.setRemoteRepositories( remoteRepositories );
            request.setResolveDependencies( true );
            request.setRepositorySession( session.getRepositorySession() );
            return projectBuilder.build( artifact, request ).getProject();
        }
        catch ( ProjectBuildingException e )
        {
            getLog().warn( "Failed to retrieve pom for " + artifact );
            return null;
        }
    }

    private List<Artifact> getRSLDependencies( MavenProject artifactProject )
    {
        List<Artifact> swcDeps = getArtifacts( SWC, artifactProject );
        for ( Iterator<Artifact> iterator = swcDeps.iterator(); iterator.hasNext(); )
        {
            Artifact artifact = (Artifact) iterator.next();
            if ( !( RSL.equals( artifact.getScope() ) || CACHING.equals( artifact.getScope() ) ) )
            {
                iterator.remove();
            }
        }
        return swcDeps;
    }

    private String getLastRslUrls( MavenProject artifactProject )
    {
        String[] urls = CompileConfigurationLoader.getCompilerPluginSettings( artifactProject, "rslUrls" );
        if ( urls == null || urls.length == 0 )
        {
            urls = AbstractMavenMojo.DEFAULT_RSL_URLS;
        }
        return urls[urls.length - 1];
    }

    private String getRuntimeLocaleOutputPath( MavenProject artifactProject )
    {
        String runtimeLocaleOutputPath =
            CompileConfigurationLoader.getCompilerPluginSetting( artifactProject, "runtimeLocaleOutputPath" );
        if ( runtimeLocaleOutputPath == null )
        {
            runtimeLocaleOutputPath = AbstractMavenMojo.DEFAULT_RUNTIME_LOCALE_OUTPUT_PATH;
        }
        return runtimeLocaleOutputPath;
    }

    private List<Artifact> getRuntimeLocalesDependencies( MavenProject artifactProject )
    {
        String[] runtimeLocales =
            CompileConfigurationLoader.getCompilerPluginSettings( artifactProject, "runtimeLocales" );
        if ( runtimeLocales == null || runtimeLocales.length == 0 )
        {
            return Collections.emptyList();
        }

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for ( String locale : runtimeLocales )
        {
            artifacts.add( repositorySystem.createArtifactWithClassifier( artifactProject.getGroupId(),
                                                                          artifactProject.getArtifactId(),
                                                                          artifactProject.getVersion(), SWF, locale ) );
        }
        return artifacts;
    }

    private List<Artifact> getSwfArtifacts()
    {
        return getArtifacts( SWF, project );
    }

    private void performRslCopy( MavenProject artifactProject )
        throws MojoExecutionException
    {
        List<Artifact> rslDeps = getRSLDependencies( artifactProject );

        if ( rslDeps.isEmpty() )
        {
            return;
        }

        String rslUrls = getLastRslUrls( artifactProject );

        for ( Artifact rslArtifact : rslDeps )
        {
            String extension;
            if ( RSL.equals( rslArtifact.getScope() ) )
            {
                extension = SWF;
            }
            else
            {
                extension = SWZ;
            }

            rslArtifact =
                repositorySystem.createArtifactWithClassifier( rslArtifact.getGroupId(), rslArtifact.getArtifactId(),
                                                               rslArtifact.getVersion(), extension, rslArtifact.getClassifier() );
            rslArtifact = replaceWithResolvedArtifact( rslArtifact );

            File destFile = resolveRslDestination( rslUrls, rslArtifact, extension );
            File sourceFile = rslArtifact.getFile();
            copy( sourceFile, destFile );
        }
    }

    private void performRuntimeLocalesCopy( MavenProject artifactProject )
        throws MojoExecutionException
    {
        List<Artifact> deps = getRuntimeLocalesDependencies( artifactProject );

        if ( deps.isEmpty() )
        {
            return;
        }

        String runtimeLocaleOutputPath = getRuntimeLocaleOutputPath( artifactProject );

        for ( Artifact artifact : deps )
        {
            artifact = replaceWithResolvedArtifact( artifact );
            copy( artifact.getFile(), resolveRuntimeLocaleDestination( runtimeLocaleOutputPath, artifact ) );
        }
    }

    private void performSubArtifactsCopy( Artifact artifact )
        throws MojoExecutionException
    {
        MavenProject artifactProject = getProject( artifact );
        if ( artifactProject != null )
        {
            if ( copyRSL )
            {
                performRslCopy( artifactProject );
            }
            if ( copyRuntimeLocales )
            {
                performRuntimeLocalesCopy( artifactProject );
            }
        }
    }

    private String replaceContextRoot( String sample )
    {
        String absoluteWebappPath = webappDirectory.getAbsolutePath();
        if ( sample.contains( "/{contextRoot}" ) )
        {
            sample = sample.replace( "/{contextRoot}", absoluteWebappPath );
        }
        else
        {
            sample = absoluteWebappPath + "/" + sample;
        }

        return sample;
    }

    private Artifact replaceWithResolvedArtifact( Artifact artifact )
        throws MojoExecutionException
    {
        return resolve( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                        artifact.getClassifier(), artifact.getType() );
    }

    private File resolveRslDestination( String rsl, Artifact artifact, String extension )
    {
        rsl = replaceContextRoot( rsl );
        rsl = MavenUtils.interpolateRslUrl( rsl, artifact, extension, null );
        return file( rsl );
    }

    private File resolveRuntimeLocaleDestination( String runtimeLocaleOutputPath, Artifact artifact )
    {
        String path = replaceContextRoot( runtimeLocaleOutputPath );
        path = MavenUtils.getRuntimeLocaleOutputPath( path, artifact, artifact.getClassifier(), SWF );

        return new File( path ).getAbsoluteFile();
    }

}
