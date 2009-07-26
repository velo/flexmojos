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
package org.sonatype.flexmojos.htmlwrapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.sonatype.flexmojos.utilities.CompileConfigurationLoader;
import org.sonatype.flexmojos.utilities.FileInterpolationUtil;
import org.sonatype.flexmojos.utilities.MavenUtils;

import eu.cedarsoft.utils.ZipExtractor;

/**
 * This goal generate the html wrapper to Flex applications, like what is done by flex builder.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 1.0
 * @phase generate-resources
 * @goal wrapper
 * @author marvin
 */
public class HtmlWrapperMojo
    extends AbstractMojo
{

    private static final String INDEX_TEMPLATE_HTML = "index.template.html";

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter expression="${project.build}"
     * @required
     * @readonly
     */
    protected Build build;

    /**
     * The template URI.
     * <p>
     * You can point to a zip file, a folder or use one of the following embed templates:
     * <ul>
     * embed:client-side-detection
     * </ul>
     * <ul>
     * embed:client-side-detection-with-history
     * </ul>
     * <ul>
     * embed:express-installation
     * </ul>
     * <ul>
     * embed:express-installation-with-history
     * </ul>
     * <ul>
     * embed:no-player-detection
     * </ul>
     * <ul>
     * embed:no-player-detection-with-history
     * </ul>
     * To point to a zip file you must use a URI like this:
     * 
     * <pre>
     * zip:/myTemplateFolder/template.zip
     * zip:c:/myTemplateFolder/template.zip
     * </pre>
     * 
     * To point to a folder use a URI like this:
     * 
     * <pre>
     * folder:/myTemplateFolder/
     * folder:c:/myTemplateFolder/
     * </pre>
     * <p>
     * This mojo will look for <tt>index.template.html</tt> for replace parameters. <br/>
     * <br/>
     * This is ignored if running in project with war packaging.
     * 
     * @parameter default-value="embed:express-installation-with-history"
     */
    private String templateURI;

    /**
     * Used to define parameters that will be replaced. Usage:
     * 
     * <pre>
     *  &lt;parameters&gt;
     *      &lt;swf&gt;${build.finalName}&lt;/swf&gt;
     *      &lt;width&gt;100%&lt;/width&gt;
     *      &lt;height&gt;100%&lt;/height&gt;
     *  &lt;/parameters&gt;
     * </pre>
     * 
     * The following prameters wil be injected if not defined:
     * <ul>
     * title
     * </ul>
     * <ul>
     * version_major
     * </ul>
     * <ul>
     * version_minor
     * </ul>
     * <ul>
     * version_revision
     * </ul>
     * <ul>
     * swf
     * </ul>
     * <ul>
     * width
     * </ul>
     * <ul>
     * height
     * </ul>
     * <ul>
     * bgcolor
     * </ul>
     * <ul>
     * application
     * </ul>
     * If you are using a custom template, and wanna some extra parameters, this is the right place to define it. <br/>
     * <br/>
     * This is ignored if running in project with war packaging.
     * 
     * @parameter
     */
    private Map<String, String> parameters;

    /**
     * output Directory to store final html <br/>
     * <br/>
     * This is ignored if running in project with war packaging.
     * 
     * @parameter default-value="${project.build.directory}"
     */
    private File outputDirectory;

    /**
     * final name of html file
     * 
     * @parameter default-value="${project.build.finalName}"
     */
    private String htmlName;

    /**
     * output Directory to store final html <br/>
     * <br/>
     * This is ignored if running in project with war packaging.
     * 
     * @parameter default-value="${project.build.directory}/html-wrapper-template"
     */
    private File templateOutputDirectory;

    /**
     * Files to not interpolate while copying files. Usually binary files. Accepts wild cards. By default exclude all
     * swf files.
     * 
     * @parameter
     */
    private String[] templateExclusions;

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
     * An external pom that provides wrapper parameters in place of the current one.
     */
    private MavenProject sourceProject;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        String packaging = project.getPackaging();

        if ( !"swf".equals( packaging ) )
        {
            loadExternalParams();

            if ( "war".equals( packaging ) )
            {
                rewireForWar();
            }
        }

        executeInternal();
    }

    /**
     * Loads the parameters value (from plugin configuration) from an externally referenced dependency pom rather than
     * the pom for the current project.
     * 
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public void loadExternalParams()
        throws MojoExecutionException
    {
        // Fetch
        Artifact sourceArtifact = getPluginDependency( "org.sonatype.flexmojos:flexmojos-maven-plugin", "pom" );
        if ( sourceArtifact == null )
        {
            throw new MojoExecutionException(
                                              "If you are wrapping an external swf, flexmojos must be provided the swf's pom as a dependency" );
        }

        resolveArtifact( sourceArtifact );
        this.sourceProject = loadProject( sourceArtifact );

        // Does source pom contain flexmojos plugin?
        Map<String, Plugin> sourcePlugins = sourceProject.getBuild().getPluginsAsMap();
        Plugin sourceFlexmojos = sourcePlugins.get( "org.sonatype.flexmojos:flexmojos-maven-plugin" );
        if ( sourceFlexmojos == null )
        {
            throw new MojoExecutionException( "Could not locate flexmojos plugin in wrapper source pom" );
        }

        this.parameters = MavenPluginUtil.extractParameters( sourceFlexmojos );
    }

    /**
     * Insert flexmojos wrapper process into maven-war-plugin's process by re-routing its warSourceDirectory
     * configuration to this.outputDirectory and using its original warSourceDirectory as the value for this.templateURI
     * 
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    private void rewireForWar()
        throws MojoExecutionException
    {
        // Fetch war plugin configuration
        Map<String, Plugin> plugins = build.getPluginsAsMap();
        Plugin warPlugin = plugins.get( "org.apache.maven.plugins:maven-war-plugin" );
        if ( warPlugin == null )
        {
            throw new MojoExecutionException( "Flexmojos HtmlWrapperMojo could not find the war plugin" );
        }
        Xpp3DomMap config = MavenPluginUtil.getParameters( warPlugin );

        // Map this.templateURI to folder:{warPlugin.warSourceDirectory)
        String warSourceDirectory = config.get( "warSourceDirectory" );
        if ( warSourceDirectory == null )
        {
            warSourceDirectory = project.getBasedir() + "/src/main/webapp";
        }
        this.templateURI = "folder:" + warSourceDirectory;

        // Map outputDirectory/templateOutputDirectory to warPlugin.workDirectory
        // so that they don't get packaged in war accidentally
        String workDirectory = config.get( "workDirectory" );
        if ( workDirectory == null )
        {
            workDirectory = build.getDirectory() + "/war/work";
        }
        this.templateOutputDirectory = new File( workDirectory, "extracted-template" );
        this.outputDirectory = new File( workDirectory, "wrapped-template" );

        // Map warPlugin.warSourceDirectory to this.outputDirectory
        config.put( "warSourceDirectory", outputDirectory.getAbsolutePath() );
    }

    private void executeInternal()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info(
                       "flexmojos " + MavenUtils.getFlexMojosVersion()
                           + " - GNU GPL License (NO WARRANTY) - See COPYRIGHT file" );

        init();

        extractTemplate();
        copySurroundingFiles();
        copyIndexTemplate();
    }

    private void copyIndexTemplate()
        throws MojoExecutionException
    {
        File indexTemplate = new File( templateOutputDirectory, INDEX_TEMPLATE_HTML );
        if ( !indexTemplate.isFile() )
        {
            getLog().debug( "No index.template.html" );
            return;
        }
        File index = new File( outputDirectory, htmlName + ".html" );

        try
        {
            FileInterpolationUtil.copyFile( indexTemplate, index, parameters );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to write " + index, e );
        }
    }

    private void copySurroundingFiles()
        throws MojoExecutionException
    {
        try
        {
            FileInterpolationUtil.copyDirectory( templateOutputDirectory, outputDirectory, parameters,
                                                 templateExclusions );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to create templates.", e );
        }

        // XXX shouldn't copy template, but there isn't a fast fix for that right know
        File template = new File( outputDirectory, INDEX_TEMPLATE_HTML );
        if ( template.exists() )
        {
            template.delete();
        }
    }

    private void extractTemplate()
        throws MojoExecutionException
    {
        getLog().info( "Extracting template" );
        templateOutputDirectory.mkdirs();

        URI uri;
        try
        {
            if ( MavenUtils.isWindows() )
            {
                // Shake bars to avoid URI syntax problems
                templateURI = templateURI.replace( '\\', '/' );
            }
            templateURI = URIUtil.encodePath( templateURI );
            uri = new URI( templateURI );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Invalid template URI.", e );
        }

        String scheme = uri.getScheme();
        if ( "embed".equals( scheme ) )
        {
            copyEmbedTemplate( uri.getSchemeSpecificPart() );
        }
        else if ( "zip".equals( scheme ) )
        {
            copyZipTemplate( uri.getSchemeSpecificPart() );
        }
        else if ( "folder".equals( scheme ) )
        {
            copyFolderTemplate( uri.getSchemeSpecificPart() );
        }
        else
        {
            throw new MojoExecutionException( "Invalid URI scheme: " + scheme );
        }

    }

    private void copyFolderTemplate( String path )
        throws MojoExecutionException
    {
        File source = new File( path );
        if ( !source.isAbsolute() )
        {
            source = new File( project.getBasedir(), path );
        }
        if ( !source.exists() || !source.isDirectory() )
        {
            throw new MojoExecutionException( "Template folder doesn't exists. " + source );
        }

        try
        {
            FileUtils.copyDirectory( source, templateOutputDirectory,
                                     FileFilterUtils.makeSVNAware( FileFilterUtils.makeCVSAware( null ) ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy template to: " + templateOutputDirectory, e );
        }
    }

    private void copyZipTemplate( String path )
        throws MojoExecutionException
    {
        File source = new File( path );
        if ( !source.exists() || !source.isFile() )
        {
            throw new MojoExecutionException( "Zip template doesn't exists. " + source );
        }

        extractZipTemplate( templateOutputDirectory, source );
    }

    private void copyEmbedTemplate( String path )
        throws MojoExecutionException
    {
        URL url = getClass().getResource( "/templates/wrapper/" + path + ".zip" );
        File template = new File( templateOutputDirectory, "template.zip" );
        try
        {
            FileUtils.copyURLToFile( url, template );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy template to: " + template, e );
        }
        extractZipTemplate( templateOutputDirectory, template );
    }

    private void extractZipTemplate( File outputDir, File template )
        throws MojoExecutionException
    {
        try
        {
            ZipExtractor ze = new ZipExtractor( template );
            ze.extract( outputDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "An error happens when trying to extract html-template.", e );
        }
    }

    /**
     * Searches the plugin local dependencies (i.e. only dependencies declared in the current plugin XML descriptor) for
     * the first dependency of the given type and then return its artifact. If type is null, then the artifact for the
     * first dependency is returned.
     * 
     * @param pluginId
     * @param type
     * @return
     */
    private Artifact getPluginDependency( String pluginId, String type )
    {
        Plugin currentPlugin = (Plugin) build.getPluginsAsMap().get( pluginId );
        for ( Iterator<Dependency> iter = currentPlugin.getDependencies().iterator(); iter.hasNext(); )
        {
            Dependency dependency = iter.next();
            if ( type == null || type.equals( dependency.getType() ) )
            {
                return artifactFactory.createArtifact( dependency.getGroupId(), dependency.getArtifactId(),
                                                       dependency.getVersion(), dependency.getClassifier(),
                                                       dependency.getType() );
            }
        }

        return null;
    }

    /**
     * Attempts to find the provided artifact within the current repository path
     * 
     * @param artifact
     * @throws MojoExecutionException
     */
    private void resolveArtifact( Artifact artifact )
        throws MojoExecutionException
    {
        try
        {
            resolver.resolve( artifact, remoteRepositories, localRepository );
        }
        catch ( ArtifactResolutionException ex )
        {
            throw new MojoExecutionException( "Could not resolve wrapper source pom artifact:  " + artifact.getId(), ex );
        }
        catch ( ArtifactNotFoundException ex )
        {
            throw new MojoExecutionException( "Could not find wrapper source pom artifact" + artifact.getId(), ex );
        }
    }

    /**
     * Tries to construct project for the provided artifact
     * 
     * @param artifact
     * @return MavenProject for the given artifact
     * @throws MojoExecutionException
     */
    private MavenProject loadProject( Artifact artifact )
        throws MojoExecutionException
    {
        try
        {
            return mavenProjectBuilder.buildFromRepository( artifact, remoteRepositories, localRepository );
        }
        catch ( ProjectBuildingException ex )
        {
            throw new MojoExecutionException( "Problems building project for:  " + artifact.getId(), ex );
        }
    }

    private void init()
    {
        /*
         * If sourceProject is defined, then parameters are from an external project and that project (sourceProject)
         * should be used as reference for default values rather than this project.
         */
        MavenProject project = this.project;
        if ( sourceProject != null )
        {
            project = sourceProject;
        }

        if ( parameters == null )
        {
            parameters = new HashMap<String, String>();
        }

        if ( !parameters.containsKey( "title" ) )
        {
            parameters.put( "title", project.getName() );
        }

        String targetPlayer = CompileConfigurationLoader.getCompilerPluginSetting( project, "targetPlayer" );
        String[] nodes = targetPlayer != null ? targetPlayer.split( "\\." ) : new String[] { "9", "0", "0" };
        if ( !parameters.containsKey( "version_major" ) )
        {
            parameters.put( "version_major", nodes[0] );
        }
        if ( !parameters.containsKey( "version_minor" ) )
        {
            parameters.put( "version_minor", nodes[1] );
        }
        if ( !parameters.containsKey( "version_revision" ) )
        {
            parameters.put( "version_revision", nodes[2] );
        }
        if ( !parameters.containsKey( "swf" ) )
        {
            parameters.put( "swf", project.getBuild().getFinalName() );
        }
        if ( !parameters.containsKey( "width" ) )
        {
            parameters.put( "width", "100%" );
        }
        if ( !parameters.containsKey( "height" ) )
        {
            parameters.put( "height", "100%" );
        }
        if ( !parameters.containsKey( "application" ) )
        {
            parameters.put( "application", project.getArtifactId() );
        }
        if ( !parameters.containsKey( "bgcolor" ) )
        {
            parameters.put( "bgcolor", "#869ca7" );
        }

        if ( templateExclusions == null )
        {
            templateExclusions = new String[] { "*.swf", "*.png", "*.jpg", "*.pdf" };
        }
    }

}
