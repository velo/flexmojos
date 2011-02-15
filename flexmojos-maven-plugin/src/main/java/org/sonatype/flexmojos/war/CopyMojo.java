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
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.flexmojos.MavenMojo;
import org.sonatype.flexmojos.common.FlexExtension;
import org.sonatype.flexmojos.common.FlexScopes;
import org.sonatype.flexmojos.compiler.AbstractCompilerMojo;
import org.sonatype.flexmojos.utilities.CompileConfigurationLoader;
import org.sonatype.flexmojos.utilities.MavenUtils;

/**
 * Goal to copy flex artifacts into war projects.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.0
 * @goal copy-flex-resources
 * @phase process-resources
 * @requiresDependencyResolution runtime
 */
public class CopyMojo
    extends AbstractMojo
    implements FlexScopes, FlexExtension, MavenMojo
{

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * LW : needed for expression evaluation The maven MojoExecution needed for ExpressionEvaluation
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession context;
    
    /** 
     * @component 
     * @required
     * @readonly
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * @parameter default-value="true"
     */
    private boolean copyRSL;

    /**
     * @parameter default-value="true"
     */
    private boolean copyRuntimeLocales;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @component
     */
    private MavenProjectBuilder mavenProjectBuilder;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     * @readonly
     */
    private List<?> remoteRepositories;

    /**
     * @component
     */
    private ArtifactResolver resolver;

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
        if ( !( "war".equals( packaging ) || "swf".equals( packaging ) ) )
        {
            getLog().warn( "'copy-flex-resources' was intended to run on war or swf project" );
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
        
        performRslCopy( project );

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

    private String[] getRslUrls( MavenProject artifactProject )
    {
        String[] urls = CompileConfigurationLoader.getCompilerPluginSettings( artifactProject, "rslUrls" );
        if ( urls == null )
        {
            urls = AbstractCompilerMojo.DEFAULT_RSL_URLS;
        }
        return urls;
    }

    private String getRuntimeLocaleOutputPath( MavenProject artifactProject )
    {
        String runtimeLocaleOutputPath =
            CompileConfigurationLoader.getCompilerPluginSetting( artifactProject, "runtimeLocaleOutputPath" );
        if ( runtimeLocaleOutputPath == null )
        {
            runtimeLocaleOutputPath = AbstractCompilerMojo.DEFAULT_RUNTIME_LOCALE_OUTPUT_PATH;
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

    public MavenSession getSession()
    {
        return context;
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
            if ( rslArtifact.getVersion() == null )
            {
                try 
                {
                    List<ArtifactVersion> versions = artifactMetadataSource.retrieveAvailableVersions(rslArtifact, localRepository, remoteRepositories);
                    rslArtifact.setVersion( rslArtifact.getVersionRange().matchVersion( versions ).toString() );
                } 
                catch ( Exception e )
                {
                    throw new MojoExecutionException( e.getMessage(), e );
                }
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

    private File resolveRuntimeLocaleDestination( String runtimeLocaleOutputPath, Artifact artifact )
    {
        String path = replaceContextRoot( runtimeLocaleOutputPath );
        path = MavenUtils.getRuntimeLocaleOutputPath( path, artifact, artifact.getClassifier(), SWF );

        return new File( path ).getAbsoluteFile();
    }
}
