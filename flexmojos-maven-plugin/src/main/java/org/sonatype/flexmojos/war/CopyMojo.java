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
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
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
     * Use final name if/when available
     * 
     * @parameter default-value="true"
     */
    private boolean useFinalName;

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
            rslArtifact = replaceWithResolvedArtifact( rslArtifact );

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
            artifact = replaceWithResolvedArtifact( artifact );
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
        throws MojoExecutionException
    {
        MavenProject pomProject = getProject( artifact );
        String fileName;
        if ( !useFinalName )
        {
            String classifier = StringUtils.isEmpty( artifact.getClassifier() ) ? "" : "-" + artifact.getClassifier();
            String version = stripVersion ? "" : "-" + artifact.getVersion();
            fileName = artifact.getArtifactId() + version + classifier + "." + SWF;
        }
        else
        {
            fileName = pomProject.getBuild().getFinalName() + "." + SWF;
        }

        if ( stripVersion && fileName.contains( artifact.getVersion() ) )
        {
            fileName = fileName.replace( "-" + artifact.getVersion(), "" );
        }

        File destFile = new File( webappDirectory, fileName );

        return destFile;
    }

    private List<Artifact> getSwfArtifacts()
    {
        return getArtifacts( SWF, project );
    }

    private List<Artifact> getAirArtifacts()
    {
        return getArtifacts( AIR, project );
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

    private Artifact replaceWithResolvedArtifact( Artifact artifact )
        throws MojoExecutionException
    {
        return MavenUtils.resolveArtifact( project, artifact, resolver, localRepository, remoteRepositories );
    }
}
