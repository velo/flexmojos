/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.asdoc;

import static org.sonatype.flexmojos.common.FlexExtension.AIR;
import static org.sonatype.flexmojos.common.FlexExtension.POM;
import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.flexmojos.MavenMojo;
import org.sonatype.flexmojos.compatibilitykit.FlexCompatibility;
import org.sonatype.flexmojos.compatibilitykit.FlexMojo;
import org.sonatype.flexmojos.test.util.PathUtil;
import org.sonatype.flexmojos.utilities.CompileConfigurationLoader;
import org.sonatype.flexmojos.utilities.FDKConfigResolver;
import org.sonatype.flexmojos.utilities.MavenUtils;
import org.sonatype.flexmojos.utilities.Namespace;

import eu.cedarsoft.utils.ZipExtractor;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.ASDoc;

/**
 * Goal which generates documentation from the ActionScript sources.
 * 
 * @goal asdoc
 * @requiresDependencyResolution
 */
public class AsDocMojo
    extends AbstractMojo
    implements FlexMojo, MavenMojo
{

    /**
     * If true, will treat multi-modules projects as only one project otherwise will generate Asdoc per project
     * 
     * @parameter default-value="false" expression="${asdoc.aggregate}"
     * @since 3.5
     */
    protected boolean aggregate;

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * @parameter expression="${project.build}"
     * @required
     * @readonly
     */
    protected Build build;

    /**
     * specifies a compatibility version. e.g. compatibility 2.0.1
     * 
     * @parameter
     */
    private String compatibilityVersion;

    /**
     * Load a file containing configuration options If not defined, by default will search for one on resources folder.
     * 
     * @parameter
     * @deprecated Use configFiles instead
     */
    protected File configFile;

    /**
     * Load a file containing configuration options If not defined, by default will search for one on resources folder.
     * 
     * @parameter
     */
    protected File[] configFiles;

    /**
     * LW : needed for expression evaluation The maven MojoExecution needed for ExpressionEvaluation
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession context;

    private Set<Artifact> dependencyArtifacts;

    /**
     * A list of classes to document. These classes must be in the source path. This is the default option. This option
     * works the same way as does the -include-classes option for the compc component compiler. For more information,
     * see Using compc, the component compiler.
     * 
     * @parameter
     */
    private String[] docClasses;

    /**
     * A list of URIs whose classes should be documented. The classes must be in the source path. You must include a URI
     * and the location of the manifest file that defines the contents of this namespace. This option works the same way
     * as does the -include-namespaces option for the compc component compiler. For more information, see Using compc,
     * the component compiler.
     * 
     * @parameter
     */
    private Namespace[] docNamespaces;

    /**
     * A list of files that should be documented. If a directory name is in the list, it is recursively searched. This
     * option works the same way as does the -include-sources option for the compc component compiler. For more
     * information, see Using compc, the component compiler.
     * 
     * @parameter
     */
    private File[] docSources;

    /**
     * A list of classes that should not be documented. You must specify individual class names. Alternatively, if the
     * ASDoc comment for the class contains the
     * 
     * @private tag, is not documented.
     * @parameter
     */
    private String[] excludeClasses;

    /**
     * Whether all dependencies found by the compiler are documented. If true, the dependencies of the input classes are
     * not documented. The default value is false.
     * 
     * @parameter default-value="false"
     */
    private boolean excludeDependencies;

    private File fontsSnapshot;

    /**
     * The text that appears at the bottom of the HTML pages in the output documentation.
     * 
     * @parameter
     */
    private String footer;

    /**
     * Sets the compiler when it runs on a server without a display. This is equivalent to using the
     * <code>compiler.headless-server</code> option of the mxmlc or compc compilers. that value determines if the
     * compiler is running on a server without a display.
     * 
     * @parameter default-value="false"
     */
    private boolean headlessServer;

    /**
     * An integer that changes the width of the left frameset of the documentation. You can change this size to
     * accommodate the length of your package names. The default value is 210 pixels.
     * 
     * @parameter default-value="210"
     */
    private int leftFramesetWidth;

    /**
	 *
	 */
    private List<File> libraries;

    /**
     * @parameter expression="${localRepository}"
     */
    protected ArtifactRepository localRepository;

    /**
     * The text that appears at the top of the HTML pages in the output documentation. The default value is
     * "API Documentation".
     * 
     * @parameter default-value="API Documentation"
     */
    private String mainTitle;

    /**
     * @component
     */
    protected MavenProjectBuilder mavenProjectBuilder;

    /**
     * Specify a URI to associate with a manifest of components for use as MXML elements.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;namespaces&gt;
     *  &lt;namespace&gt;
     *   &lt;uri&gt;http://www.adobe.com/2006/mxml&lt;/uri&gt;
     *   &lt;manifest&gt;${basedir}/manifest.xml&lt;/manifest&gt;
     *  &lt;/namespace&gt;
     * &lt;/namespaces&gt;
     * </pre>
     * 
     * @parameter
     */
    private Namespace[] namespaces;

    /**
     * The output directory for the generated documentation.
     * 
     * @parameter default-value="${project.build.directory}/asdoc"
     */
    protected File outputDirectory;

    /**
     * The descriptions to use when describing a package in the documentation. You can specify more than one package
     * option.
     * 
     * @parameter
     */
    private Map<String, String> packageDescriptions;

    /**
     * @parameter expression="${plugin.artifacts}"
     */
    private List<Artifact> pluginArtifacts;

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    /**
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     * @since 3.5
     */
    protected List<MavenProject> reactorProjects;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    @SuppressWarnings( "unchecked" )
    protected List remoteRepositories;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    /**
     * List of path elements that form the roots of ActionScript class hierarchies.
     * 
     * @parameter
     */
    protected File[] sourcePaths;

    /**
     * The path to the ASDoc template directory. The default is the asdoc/templates directory in the ASDoc installation
     * directory. This directory contains all the HTML, CSS, XSL, and image files used for generating the output.
     * 
     * @parameter
     */
    private File templatesPath;

    /**
     * The text that appears in the browser window in the output documentation. The default value is
     * "API Documentation".
     * 
     * @parameter default-value="API Documentation"
     */
    private String windowTitle;

    private void addCompatibility( List<String> args )
    {
        if ( compatibilityVersion != null )
        {
            args.add( "-compiler.mxml.compatibility-version=" + compatibilityVersion );
        }
    }

    private void addDefines( List<String> args )
    {
        // Read defines from flex-compiler
        Xpp3Dom defines = CompileConfigurationLoader.getCompilerPluginConfiguration( project, "defines" );
        if ( defines == null || defines.getChildren() == null || defines.getChildren().length == 0 )
        {
            return;
        }

        for ( Xpp3Dom define : defines.getChildren() )
        {
            args.add( "-compiler.define+=" + define.getName() + "," + define.getValue() );
        }

    }

    private void addDocClasses( List<String> args )
    {
        if ( docClasses == null || docClasses.length == 0 )
        {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for ( String docClass : docClasses )
        {
            if ( sb.length() == 0 )
            {
                sb.append( "-doc-classes=" );
            }
            else
            {
                sb.append( ',' );
            }

            sb.append( docClass );
        }
        args.add( sb.toString() );
    }

    private void addDocNamespaces( List<String> args )
    {
        if ( docNamespaces == null || docNamespaces.length == 0 )
        {
            return;
        }

        // -compiler.namespaces.namespace <uri> <manifest>
        // alias -namespace
        // Specify a URI to associate with a manifest of components for use as
        // MXML elements (repeatable)

        for ( Namespace namespace : docNamespaces )
        {
            args.add( "-compiler.namespaces.namespace" );
            args.add( namespace.getUri() );
            args.add( namespace.getManifest().getAbsolutePath() );
        }

        // -doc-namespaces [uri] [...]
        // alias -dn
        // (repeatable)
        StringBuilder sb = new StringBuilder();
        for ( Namespace namespace : docNamespaces )
        {
            if ( sb.length() == 0 )
            {
                sb.append( "-doc-namespaces=" );
            }
            else
            {
                sb.append( ',' );
            }

            sb.append( namespace.getUri() );
        }
        args.add( sb.toString() );
    }

    private void addDocSources( List<String> args )
    {
        if ( docSources == null || docSources.length == 0 )
        {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for ( File docs : docSources )
        {
            if ( sb.length() == 0 )
            {
                sb.append( "-doc-sources=" );
            }
            else
            {
                sb.append( ',' );
            }

            sb.append( docs.getAbsolutePath() );
        }
        args.add( sb.toString() );
    }

    private void addExcludeClasses( List<String> args )
    {
        if ( excludeClasses == null || excludeClasses.length == 0 )
        {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for ( String excludeClass : excludeClasses )
        {
            if ( sb.length() == 0 )
            {
                sb.append( "-exclude-classes=" );
            }
            else
            {
                sb.append( ',' );
            }

            sb.append( excludeClass );
        }

        args.add( sb.toString() );
    }

    protected void addExtraArgs( List<String> args )
    {
        // meant to be overwritten
    }

    private void addFooter( List<String> args )
    {
        if ( footer != null )
        {
            args.add( "-footer" );
            args.add( footer );
        }
    }

    private void addLibraries( List<String> args )
    {
        if ( libraries == null || libraries.size() == 0 )
        {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for ( File lib : libraries )
        {
            if ( sb.length() == 0 )
            {
                sb.append( "-library-path=" );
            }
            else
            {
                sb.append( ',' );
            }

            sb.append( lib.getAbsolutePath() );
        }

        args.add( sb.toString() );
    }

    private void addNamespaces( List<String> args )
    {
        if ( namespaces == null || namespaces.length == 0 )
        {
            return;
        }

        // -compiler.namespaces.namespace <uri> <manifest>
        // alias -namespace
        // Specify a URI to associate with a manifest of components for use as
        // MXML elements (repeatable)

        for ( Namespace namespace : namespaces )
        {
            args.add( "-compiler.namespaces.namespace" );
            args.add( namespace.getUri() );
            args.add( namespace.getManifest().getAbsolutePath() );
        }
    }

    private void addPackageDescriptions( List<String> args )
    {
        if ( packageDescriptions == null )
        {
            return;
        }

        for ( String pack : packageDescriptions.keySet() )
        {
            args.add( "-packages.package" );
            args.add( pack );
            args.add( packageDescriptions.get( pack ) );
        }
    }

    private void addSourcePath( List<String> args )
    {
        if ( sourcePaths == null || sourcePaths.length == 0 )
        {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for ( File path : sourcePaths )
        {
            if ( sb.length() == 0 )
            {
                sb.append( "-source-path=" );
            }
            else
            {
                sb.append( ',' );
            }

            sb.append( path.getAbsolutePath() );
        }
        args.add( sb.toString() );
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( aggregate && !project.isExecutionRoot() )
        {
            getLog().info( "Skipping asdoc execution, running on aggregate mode." );
            return;
        }

        String packaging = project.getPackaging();
        if ( SWC.equals( packaging ) || SWF.equals( packaging ) || ( POM.equals( packaging ) && aggregate ) )
        {
            setUp();
            run();
            tearDown();
        }
        else
        {
            getLog().warn( "Invalid packaging for asdoc generation " + packaging );
        }
    }

    private File generateDefaultTemplate()
        throws MojoExecutionException
    {
        File templates = new File( build.getDirectory(), "templates" );
        templates.mkdirs();
        for ( Artifact artifact : pluginArtifacts )
        {
            if ( "template".equals( artifact.getClassifier() ) )
            {
                try
                {
                    ZipExtractor ze = new ZipExtractor( artifact.getFile() );
                    ze.extract( templates );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "An error happens when trying to extract AsDoc Template.", e );
                }
                makeHelperExecutable( templates );
                return templates;
            }
        }

        throw new MojoExecutionException( "Unable to generate default template." );
    }

    public String getCompilerVersion()
    {
        Artifact compiler = MavenUtils.searchFor( pluginArtifacts, "com.adobe.flex", "compiler", null, "pom", null );
        return compiler.getVersion();
    }

    /**
     * Returns Set of dependency artifacts which are resolved for the project.
     * 
     * @return Set of dependency artifacts.
     * @throws MojoExecutionException
     */
    protected Set<Artifact> getDependencyArtifacts()
        throws MojoExecutionException
    {
        if ( dependencyArtifacts == null )
        {
            if ( aggregate )
            {
                dependencyArtifacts = new LinkedHashSet<Artifact>();
                for ( MavenProject project : this.reactorProjects )
                {
                    dependencyArtifacts.addAll( MavenUtils.getDependencyArtifacts( project, resolver, localRepository,
                                                                                   remoteRepositories,
                                                                                   artifactMetadataSource,
                                                                                   artifactFactory ) );
                }
            }
            else
            {
                dependencyArtifacts =
                    MavenUtils.getDependencyArtifacts( project, resolver, localRepository, remoteRepositories,
                                                       artifactMetadataSource, artifactFactory );
            }
        }
        return dependencyArtifacts;
    }

    public MavenSession getSession()
    {
        return context;
    }

    @FlexCompatibility( maxVersion = "4.0.0.3127" )
    private void makeHelperExecutable( File templates )
        throws MojoExecutionException
    {
        if ( !MavenUtils.isWindows() )
        {
            getLog().info( "Making asdoc helper executable due to Flex SDK: " + getCompilerVersion() );

            Runtime runtime = Runtime.getRuntime();
            String pathname =
                String.format( "%s/%s", templates.getAbsolutePath(), "asDocHelper"
                    + ( MavenUtils.isLinux() ? ".linux" : "" ) );
            String[] statements = new String[] { "chmod", "u+x", pathname };
            try
            {
                Process p = runtime.exec( statements );
                int result = p.waitFor();
                if ( 0 != result )
                {
                    throw new MojoExecutionException( String.format( "Unable to execute %s. Return value = %d",
                                                                     Arrays.asList( statements ), result ) );
                }
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( String.format( "Unable to execute %s", Arrays.asList( statements ) ) );
            }
        }
    }

    protected void run()
        throws MojoExecutionException, MojoFailureException
    {
        List<String> args = new ArrayList<String>();

        addNamespaces( args );
        addDocSources( args );
        addDocClasses( args );
        addDocNamespaces( args );
        addSourcePath( args );
        addLibraries( args );
        addCompatibility( args );
        addPackageDescriptions( args );
        addDefines( args );
        addExcludeClasses( args );
        addFooter( args );
        args.add( "-templates-path=" + templatesPath.getAbsolutePath() );
        args.add( "-window-title=" + windowTitle );
        args.add( "-main-title=" + mainTitle );
        args.add( "-left-frameset-width=" + leftFramesetWidth );
        args.add( "-exclude-dependencies=" + excludeDependencies );
        args.add( "-compiler.fonts.local-fonts-snapshot=" + fontsSnapshot.getAbsolutePath() );
        if ( headlessServer )
        {
            args.add( "-compiler.headless-server=true" );
        }
        if ( configFile != null )
        {
            args.add( "-load-config=" + configFile.getAbsolutePath() );
        }
        else if ( configFiles != null )
        {
            String separator = "=";
            for ( File cfg : configFiles )
            {
                args.add( " -load-config" + separator + PathUtil.getCanonicalPath( cfg ) );
                separator = "+=";
            }
        }
        else
        {
            args.add( "-load-config=" );
        }
        if ( outputDirectory != null )
        {
            args.add( "-output=" + outputDirectory.getAbsolutePath() );
        }

        addExtraArgs( args );

        getLog().info( args.toString() );

        // I hate this, waiting for asdoc-oem
        // https://bugs.adobe.com/jira/browse/SDK-15405
        ASDoc.asdoc( args.toArray( new String[args.size()] ) );

        int errorCount = ThreadLocalToolkit.errorCount();
        if ( errorCount > 0 )
        {
            throw new MojoExecutionException( "Error compiling!" );
        }
    }

    @SuppressWarnings( "unchecked" )
    protected void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        if ( sourcePaths == null )
        {
            List<MavenProject> projects;
            if ( aggregate )
            {
                projects = new ArrayList<MavenProject>();
                for ( MavenProject project : this.reactorProjects )
                {
                    // filter flex projects
                    if ( SWF.equals( project.getPackaging() ) || SWC.equals( project.getPackaging() )
                        || AIR.equals( project.getPackaging() ) )
                    {
                        projects.add( project );
                    }
                }
            }
            else
            {
                projects = Arrays.asList( project );
            }

            List<File> sources = new ArrayList<File>();
            for ( MavenProject project : projects )
            {
                List<String> sourceRoots = project.getCompileSourceRoots();
                for ( String sourceRoot : sourceRoots )
                {
                    File source = new File( sourceRoot );
                    if ( source.exists() )
                    {
                        sources.add( source );
                    }
                }
            }

            sourcePaths = sources.toArray( new File[sources.size()] );
        }
        if ( docSources == null && docClasses == null && docNamespaces == null )
        {
            getLog().warn( "Nothing expecified to include.  Assuming source paths." );
            docSources = sourcePaths;
        }

        libraries = new ArrayList<File>();
        for ( Artifact artifact : getDependencyArtifacts() )
        {
            if ( SWC.equals( artifact.getType() ) )
            {
                libraries.add( artifact.getFile() );
            }
        }

        if ( outputDirectory != null )
        {
            outputDirectory.mkdirs();
        }

        if ( configFile == null )
        {
            List<Resource> resources = build.getResources();
            for ( Resource resource : resources )
            {
                File cfg = new File( resource.getDirectory(), "config.xml" );
                if ( cfg.exists() )
                {
                    configFile = cfg;
                    break;
                }
            }
        }

        if ( fontsSnapshot == null )
        {
            URL url;
            if ( MavenUtils.isMac() )
            {
                url = getClass().getResource( "/fonts/macFonts.ser" );
            }
            else
            {
                // And linux?!
                // if(os.contains("windows")) {
                url = getClass().getResource( "/fonts/winFonts.ser" );
            }
            File fontsSer = new File( build.getDirectory(), "fonts.ser" );
            try
            {
                FileUtils.copyURLToFile( url, fontsSer );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error creating fonts.ser" );
            }
            fontsSnapshot = fontsSer;
        }

        if ( templatesPath == null )
        {
            templatesPath = generateDefaultTemplate();
        }

        FDKConfigResolver sdkConfigResolver =
            new FDKConfigResolver( getDependencyArtifacts(), build, getCompilerVersion() );
        List<Namespace> fdkNamespaces = sdkConfigResolver.getNamespaces();
        // we must merge user custom namespaces and default SDK namespaces, because we not use compiler API ?
        // https://bugs.adobe.com/jira/browse/SDK-15405
        if ( fdkNamespaces != null )
        {
            if ( namespaces != null )
            {
                fdkNamespaces.addAll( Arrays.asList( namespaces ) );
            }
            namespaces = fdkNamespaces.toArray( new Namespace[fdkNamespaces.size()] );
        }
    }

    protected void tearDown()
        throws MojoExecutionException, MojoFailureException
    {

    }
}
