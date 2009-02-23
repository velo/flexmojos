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
package org.sonatype.flexmojos.war;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.common.FlexExtension;
import org.sonatype.flexmojos.common.FlexScopes;
import org.sonatype.flexmojos.compiler.AbstractFlexCompilerMojo;
import org.sonatype.flexmojos.utilities.CompileConfigurationLoader;
import org.sonatype.flexmojos.utilities.MavenUtils;

/**
 * Goal to copy flex artifacts into war projects.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.0
 * @goal copy-flex-resources
 * @phase process-resources
 * @requiresDependencyResolution compile
 * @author Marvin Froeder
 */
public class CopyMojo
    extends AbstractMojo
    implements FlexScopes, FlexExtension
{

    /**
     * @component
     */
    private MavenProjectBuilder mavenProjectBuilder;

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List<?> remoteRepositories;

    /**
     * The directory where the webapp is built.
     * 
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private File webappDirectory;

    /**
     * Strip artifact version during copy
     * 
     * @parameter default-value="false"
     */
    private boolean stripVersion;

    /**
     * @parameter default-value="true"
     */
    private boolean copyRSL;

    /**
     * @parameter default-value="true"
     */
    private boolean copyRuntimeLocales;

    /**
     * Skip mojo execution
     * 
     * @parameter default-value="false" expression="${flexmojos.copy.skip}"
     */
    private boolean skip;

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
            getLog().warn( "Unable to copy flex resources to a non war project" );
            return;
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
    }

    private void performSubArtifactsCopy( Artifact artifact )
        throws MojoExecutionException
    {
        MavenProject artifactProject = getProject( artifact );
        if ( artifactProject != null )
        {
            try
            {
                artifactProject.setArtifacts( artifactProject.createArtifacts( artifactFactory, null, null ) );
            }
            catch ( InvalidDependencyVersionException e )
            {
                throw new MojoExecutionException( "Error resolving artifacts " + artifact, e );
            }

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

    private void performRslCopy( MavenProject artifactProject )
        throws MojoExecutionException
    {
        List<Artifact> rslDeps = getRSLDependencies( artifactProject );

        if ( rslDeps.isEmpty() )
        {
            return;
        }

        String[] rslUrls = getRslUrls( artifactProject );

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
                artifactFactory.createArtifactWithClassifier( rslArtifact.getGroupId(), rslArtifact.getArtifactId(),
                                                              rslArtifact.getVersion(), extension, null );

            try
            {
                resolver.resolve( rslArtifact, remoteRepositories, localRepository );
            }
            catch ( AbstractArtifactResolutionException e )
            {
                throw new MojoExecutionException( "Error resolving artifacts " + rslArtifact, e );
            }

            File[] destFiles = resolveRslDestination( rslUrls, rslArtifact, extension );
            File sourceFile = rslArtifact.getFile();

            for ( File destFile : destFiles )
            {
                copy( sourceFile, destFile );
            }
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
            try
            {
                resolver.resolve( artifact, remoteRepositories, localRepository );
            }
            catch ( AbstractArtifactResolutionException e )
            {
                throw new MojoExecutionException( "Error resolving artifacts " + artifact, e );
            }

            copy( artifact.getFile(), resolveRuntimeLocaleDestination( runtimeLocaleOutputPath, artifact ) );
        }
    }

    private File[] resolveRslDestination( String[] rslUrls, Artifact artifact, String extension )
    {
        File[] rsls = new File[rslUrls.length];
        for ( int i = 0; i < rslUrls.length; i++ )
        {
            String rsl = replaceContextRoot( rslUrls[i] );
            rsl = MavenUtils.getRslUrl( rsl, artifact, extension );
            rsls[i] = new File( rsl ).getAbsoluteFile();
        }
        return rsls;
    }

    private String[] getRslUrls( MavenProject artifactProject )
    {
        String[] urls = CompileConfigurationLoader.getCompilerPluginSettings( artifactProject, "rslUrls" );
        if ( urls == null )
        {
            urls = AbstractFlexCompilerMojo.DEFAULT_RSL_URLS;
        }
        return urls;
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

    private File resolveRuntimeLocaleDestination( String runtimeLocaleOutputPath, Artifact artifact )
    {
        String path = replaceContextRoot( runtimeLocaleOutputPath );
        path = MavenUtils.getRuntimeLocaleOutputPath( path, artifact, artifact.getClassifier(), SWF );

        return new File( path ).getAbsoluteFile();
    }

    private String getRuntimeLocaleOutputPath( MavenProject artifactProject )
    {
        String runtimeLocaleOutputPath =
            CompileConfigurationLoader.getCompilerPluginSetting( artifactProject, "runtimeLocaleOutputPath" );
        if ( runtimeLocaleOutputPath == null )
        {
            runtimeLocaleOutputPath = AbstractFlexCompilerMojo.DEFAULT_RUNTIME_LOCALE_OUTPUT_PATH;
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
            artifacts.add( artifactFactory.createArtifactWithClassifier( artifactProject.getGroupId(),
                                                                         artifactProject.getArtifactId(),
                                                                         artifactProject.getVersion(), SWF, locale ) );
        }
        return artifacts;
    }

    private MavenProject getProject( Artifact artifact )
        throws MojoExecutionException
    {
        try
        {
            MavenProject pomProject =
                mavenProjectBuilder.buildFromRepository( artifact, remoteRepositories, localRepository );
            return pomProject;
        }
        catch ( ProjectBuildingException e )
        {
            getLog().warn( "Failed to retrieve pom for " + artifact );
            return null;
        }
    }

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

    private File getDestinationFile( Artifact artifact )
    {
        File destFile;
        if ( stripVersion )
        {
            destFile = new File( webappDirectory, artifact.getArtifactId() + "." + SWF );
        }
        else
        {
            destFile = new File( webappDirectory, artifact.getArtifactId() + "-" + artifact.getVersion() + "." + SWF );
        }
        return destFile;
    }

    private List<Artifact> getSwfArtifacts()
    {
        return getArtifacts( SWF, project );
    }

    @SuppressWarnings( "unchecked" )
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
}
