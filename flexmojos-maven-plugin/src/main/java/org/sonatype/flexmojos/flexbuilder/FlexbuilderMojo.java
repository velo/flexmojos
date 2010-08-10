/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.flexbuilder;

import static org.sonatype.flexmojos.common.FlexExtension.AIR;
import static org.sonatype.flexmojos.common.FlexExtension.RB_SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.common.FlexExtension.SWF;
import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.isMinVersionOK;
import static org.sonatype.flexmojos.compatibilitykit.VersionUtils.splitVersion;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.ide.IdeDependency;
import org.apache.maven.plugin.ide.IdeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.sonatype.flexmojos.common.FlexScopes;
import org.sonatype.flexmojos.compatibilitykit.FlexMojo;
import org.sonatype.flexmojos.flexbuilder.sdk.LinkType;
import org.sonatype.flexmojos.flexbuilder.sdk.LocalSdk;
import org.sonatype.flexmojos.flexbuilder.sdk.LocalSdkEntry;
import org.sonatype.flexmojos.test.util.PathUtil;
import org.sonatype.flexmojos.utilities.HtmlWrapperUtil;
import org.sonatype.flexmojos.utilities.MavenUtils;
import org.sonatype.flexmojos.utilities.Namespace;
import org.sonatype.flexmojos.utilities.SourceFileResolver;

/**
 * Generates Flex Builder configuration files for SWC and SWF projects.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.0
 * @extendsPlugin eclipse
 * @extendsGoal eclipse
 * @goal flexbuilder
 * @requiresDependencyResolution test
 */
public class FlexbuilderMojo
    extends AbstractIdeMojo implements FlexMojo
{

    static final String APPLICATION_NATURE = "com.adobe.flexbuilder.project.flexnature";

    static final String LIBRARY_NATURE = "com.adobe.flexbuilder.project.flexlibnature";

    static final String ACTIONSCRIPT_NATURE = "com.adobe.flexbuilder.project.actionscriptnature";

    static final String FLEXBUILDER_AIR_NATURE = "com.adobe.flexbuilder.apollo.apollonature";

    static final String FLEXBUILDER_BUILD_COMMAND = "com.adobe.flexbuilder.project.flexbuilder";

    static final String AIR_BUILD_COMMAND = "com.adobe.flexbuilder.apollo.apollobuilder";

    /**
     * Additional application files. The paths must be relative to the source folder.
     * 
     * @parameter
     * @alias "applications"
     */
    protected List<String> additionalApplications;
    
    /**
     * List of css files that will be compiled into swfs within Eclipse. The path must be relative to the base directory
     * of the project. Usage:
     * 
     * <pre>
     * &lt;buildCssFiles&amp;gt
     *     &lt;path&gt;src/style/main.css&lt;path&gt;
     * &lt;/buildCssFiles&gt;
     * </pre>
     * 
     * @parameter
     */
    protected String[] buildCssFiles;
    
    /**
     * @parameter default-value="true" expression="${enableFlexBuilderBuildCommand}"
     */
    protected boolean enableFlexBuilderBuildCommand;
    
    /**
     * Local SDK information used to help with better integration
     * between Flex Mojos and Flex/Flash Builder local SDKs.
     */
    protected LocalSdk localSdk;
    
    /**
     * @parameter default-value="false" expression="${generateHtmlWrapper}"
     */
    protected boolean generateHtmlWrapper;
    
    /**
     * Customize the outputFolderPath of the Eclipse FlexBuilder/FlashBuilder Compiler.
     * 
     * @parameter default-value="bin-debug"
     */
    protected String ideOutputFolderPath;

    /**
     * Directory path where the html template files will be copied to.
     *
     * Since Flex Builder is hard coded to ${basedir}/html-template there
     * should be no reason to change this.
     *
     * @parameter default-value="${basedir}/html-template"
     */
    protected File ideTemplateOutputFolder;
    
    /**
     * Specifies whether this project should be treated as a Flex or a pure ActionScript project by Flexbuilder. If set
     * to true:
     * <ul>
     * <li>Removes Flex (app/lib) natures from <code>.project</code> file.</li>
     * <li>Changes exclusions from library path entries in <code>.actionScriptProperties</code> file.</li>
     * <li>Completly omits creation of the <code>.flexProperties</code> file.</li>
     * </ul>
     * If not defined Flexmojos will lockup for com.adobe.flex.framework:framework:swc and set as <i>true</i> if found
     * or <i>false</i> if not found
     * 
     * @parameter
     */
    protected Boolean pureActionscriptProject;


    /* Start Duplicated */

    /**
     * Turn on generation of accessible SWFs.
     * 
     * @parameter default-value="false"
     */
    protected boolean accessible;
    
    /**
     * This is equilvalent to the <code>compiler.mxmlc.compatibility-version</code> option of the compc compiler. Must
     * be in the form <major>.<minor>.<revision> Valid values: <tt>2.0.0</tt>, <tt>2.0.1</tt> and <tt>3.0.0</tt>
     * 
     * @see http://livedocs.adobe.com/flex/3/html/help.html?content=versioning_4. html
     * @parameter
     */
    protected String compatibilityVersion;
    
    /**
     * Load a file containing configuration options If not defined, by default will search for one on resources folder.
     * 
     * @parameter
     */
    protected List<File> configFiles;
    
    /**
     * Context root to pass to the compiler.
     * 
     * @parameter
     */
    protected String contextRoot;
    
    /**
     * Sets the default application width in pixels. This is equivalent to using the <code>default-size</code> option of
     * the mxmlc or compc compilers.
     * 
     * @parameter default-value="500"
     */
    protected int defaultSizeWidth;
    
    /**
     * Sets the default application height in pixels. This is equivalent to using the <code>default-size</code> option
     * of the mxmlc or compc compilers.
     * 
     * @parameter default-value="375"
     */
    protected int defaultSizeHeight;
    
    /**
     * defines: specifies a list of define directive key and value pairs. For example, CONFIG::debugging<BR>
     * Usage:
     * 
     * <pre>
     * &lt;definesDeclaration&gt;
     *   &lt;property&gt;
     *     &lt;name&gt;SOMETHING::aNumber&lt;/name&gt;
     *     &lt;value&gt;2.2&lt;/value&gt;
     *   &lt;/property&gt;
     *   &lt;property&gt;
     *     &lt;name&gt;SOMETHING::aString&lt;/name&gt;
     *     &lt;value&gt;&quot;text&quot;&lt;/value&gt;
     *   &lt;/property&gt;
     * &lt;/definesDeclaration&gt;
     * </pre>
     * 
     * @parameter
     */
    protected Properties definesDeclaration;

    /**
     * Keep the following AS3 metadata in the bytecodes.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;keepAs3Metadatas&gt;
     *   &lt;keepAs3Metadata&gt;Bindable&lt;/keepAs3Metadata&gt;
     *   &lt;keepAs3Metadata&gt;Events&lt;/keepAs3Metadata&gt;
     * &lt;/keepAs3Metadatas&gt;
     * </pre>
     * 
     * @parameter
     */
    protected String[] keepAs3Metadatas;

    /**
     * Sets the locales that the compiler uses to replace <code>{locale}</code> tokens that appear in some configuration
     * values. This is equivalent to using the <code>compiler.locale</code> option of the mxmlc or compc compilers. <BR>
     * Usage:
     * 
     * <pre>
     * &lt;locales&gt;
     *    &lt;locale&gt;en_US&lt;/locale&gt;
     *    &lt;locale&gt;pt_BR&lt;/locale&gt;
     *    &lt;locale&gt;es_ES&lt;/locale&gt;
     * &lt;/locales&gt;
     * </pre>
     * 
     * @parameter
     * @deprecated
     */
    protected String[] locales;

    /**
     * Default locale for libraries. This is useful to non localized applications, just to define swc.rb locale
     * 
     * @parameter default-value="en_US"
     */
    protected String defaultLocale;
    
    /**
     * The greeting to display.
     * 
     * @parameter services default-value="true"
     */
    protected boolean incremental;
    
    /**
     * Automatically scans the paths looking for compile units (.as and .mxml files) adding the represented classes with
     * the <code>include-classes</code> option.
     * <p>
     * This option is useful if you want to compile as with <code>includeClasses</code> option without the need to
     * manually maintain the class list in the pom.
     * </p>
     * <p>
     * Specify <code>includes</code> parameter to include different compile units than the default .as and .mxml ones.
     * Specify <code>excludes</code> parameter to exclude compile units that would otherwise be included.
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeAsClasses&gt;
     *   &lt;sources&gt;
     *     &lt;directory&gt;${baseDir}/src/main/flex&lt;/directory&gt;
     *     &lt;excludes&gt;
     *       &lt;exclude&gt;&#042;&#042;/&#042;Incl.as&lt;/exclude&gt;
     *     &lt;/excludes&gt;
     *   &lt;/sources&gt;
     * &lt;/includeAsClasses&gt;
     * </pre>
     * 
     * @parameter
     */
    protected FileSet[] includeAsClasses;
    
    /**
     * This is the equilvalent of the <code>include-classes</code> option of the compc compiler.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;includeClassses&gt;
     *   &lt;class&gt;foo.Bar&lt;/class&gt;
     * &lt;/includeClasses&gt;
     * </pre>
     * 
     * @parameter
     */
    protected String[] includeClasses;
    
    /**
     * This is equilvalent to the <code>include-namespaces</code> option of the compc compiler.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;includeNamespaces&gt;
     *   &lt;namespace&gt;http://www.adobe.com/2006/mxml&lt;/namespace&gt;
     * &lt;/includeNamespaces&gt;
     * </pre>
     * 
     * @parameter
     */
    protected String[] includeNamespaces;

    /**
     * This is the equilvalent of the <code>include-sources</code> option of the compc compiler.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;includeSources&gt;
     *   &lt;sources&gt;${baseDir}/src/main/flex&lt;/sources&gt;
     * &lt;/includeSources&gt;
     * </pre>
     * 
     * @parameter
     */
    protected File[] includeSources;

    /**
     * This is equivalent to the <code>include-file</code> option of the compc compiler.<BR>
     * Usage:
     *
     * <pre>
     * &lt;includeFiles&gt;
     * &lt;file&gt;${baseDir}/anyFile.txt&lt;/file&gt;
     * &lt;/includeFiles&gt;
     * </pre>
     *
     * @parameter
     */
    protected File[] includeFiles;
    
    /**
     * Instructs the compiler to keep a style sheet's type selector in a SWF file, even if that type (the class) is not
     * used in the application. This is equivalent to using the <code>compiler.keep-all-type-selectors</code> option of
     * the mxmlc or compc compilers.
     * 
     * @parameter default-value="false"
     */
    protected boolean keepAllTypeSelectors;
    
    /**
     * The list of modules files to be compiled. The path must be relative with source folder.<BR>
     * This will create a modules entry in .actionScriptProperties Usage:
     * 
     * <pre>
     * &lt;moduleFiles&gt;
     *   &lt;module&gt;com/acme/AModule.mxml&lt;/module&gt;
     * &lt;/moduleFiles&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] moduleFiles;
    
    /**
     * Specify a URI to associate with a manifest of components for use as MXML elements.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;namespaces&gt;
     *   &lt;namespace&gt;
     *     &lt;uri&gt;http://www.adobe.com/2006/mxml&lt;/uri&gt;
     *     &lt;manifest&gt;${basedir}/manifest.xml&lt;/manifest&gt;
     *   &lt;/namespace&gt;
     * &lt;/namespaces&gt;
     * </pre>
     * 
     * @parameter
     */
    protected Namespace[] namespaces;
    
    /**
     * policyFileUrls array of policy file URLs. Each entry in the rslUrls array must have a corresponding entry in this
     * array. A policy file may be needed in order to allow the player to read an RSL from another domain. If a policy
     * file is not required, then set it to an empty string. Accept some special tokens:
     * 
     * <pre>
     * {contextRoot}        - replace by defined context root
     * {groupId}            - replace by library groupId
     * {artifactId}         - replace by library artifactId
     * {version}            - replace by library version
     * {extension}          - replace by library extension swf or swz
     * </pre>
     * 
     * <BR>
     * Usage:
     * 
     * <pre>
     * &lt;policyFileUrls&gt;
     *   &lt;url&gt;/{contextRoot}/rsl/policy-{artifactId}-{version}.xml&lt;/url&gt;
     * &lt;/policyFileUrls&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] policyFileUrls;
    
    /**
     * rslUrls array of URLs. The first RSL URL in the list is the primary RSL. The remaining RSL URLs will only be
     * loaded if the primary RSL fails to load. Accept some special tokens:
     * 
     * <pre>
     * {contextRoot}        - replace by defined context root
     * {groupId}            - replace by library groupId
     * {artifactId}         - replace by library artifactId
     * {version}            - replace by library version
     * {extension}          - replace by library extension swf or swz
     * </pre>
     * 
     * default-value="/{contextRoot}/rsl/{artifactId}-{version}.{extension}" <BR>
     * Usage:
     * 
     * <pre>
     * &lt;rslUrls&gt;
     *   &lt;url&gt;/{contextRoot}/rsl/{artifactId}-{version}.{extension}&lt;/url&gt;
     * &lt;/rslUrls&gt;
     * </pre>
     * 
     * @parameter
     */
    protected String[] rslUrls;
    
    /**
     * Run the AS3 compiler in a mode that detects legal but potentially incorrect code
     * 
     * @parameter default-value="true"
     */
    protected boolean showWarnings;

    /**
     * The file to be compiled. The path must be relative to the source folder.
     * 
     * @parameter
     */
    protected String sourceFile;
    
    /**
     * Run the AS3 compiler in strict error checking mode.
     * 
     * @parameter default-value="true"
     */
    protected boolean strict;
    
    /**
     * specifies the version of the player the application is targeting. Features requiring a later version will not be
     * compiled into the application. The minimum value supported is "9.0.0". If not defined will take the default value
     * from current playerglobal dependency.
     * 
     * @parameter
     */
    protected String targetPlayer;
    
    /**
     * The template URI. This is the same usage as on the wrapper mojo.
     *
     * To make this mojo copy the template URI to the templateOutputPath
     * generateHtmlWrapper must be set to true.
     *
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
     * Unlike the html wrapper mojo this mojo will only copy the template files
     * to the htmlTemplateOutputPath. From there Flex Builder will work with them
     * as normal.
     *
     * @parameter default-value="embed:express-installation-with-history"
     */
    protected String templateURI;

    /**
     * List of CSS or SWC files to apply as a theme. <>BR Usage:
     * 
     * <pre>
     * &lt;themes&gt;
     *    &lt;theme&gt;css/main.css&lt;/theme&gt;
     * &lt;/themes&gt;
     * </pre>
     * 
     * If you are using SWC theme should be better keep it's version controlled, so is advised to use a dependency with
     * theme scope.<BR>
     * Like this:
     * 
     * <pre>
     * &lt;dependency&gt;
     *   &lt;groupId&gt;com.acme&lt;/groupId&gt;
     *   &lt;artifactId&gt;acme-theme&lt;/artifactId&gt;
     *   &lt;type&gt;swc&lt;/type&gt;
     *   &lt;scope&gt;theme&lt;/scope&gt;
     *   &lt;version&gt;1.0&lt;/version&gt;
     * &lt;/dependency&gt;
     * </pre>
     * 
     * @parameter
     */
    protected String[] themes;
    
    /**
     * Sets the location of the Flex Data Services service configuration file. This is equivalent to using the
     * <code>compiler.services</code> option of the mxmlc and compc compilers. If not define will look inside resources
     * directory for services-config.xml
     * 
     * @parameter
     */
    protected File services;
    
    /**
     * Verifies the RSL loaded has the same digest as the RSL specified when the application was compiled. This is
     * equivalent to using the <code>verify-digests</code> option in the mxmlc compiler.
     * 
     * @parameter default-value="true"
     */
    protected boolean verifyDigests;

    /* End Duplicated */
    
    /**
     * @parameter default-value="true"
     */
    protected boolean htmlExpressInstall;
    
    /**
     * @parameter default-value="true"
     */
    protected boolean htmlHistoryManagement;
    
    /**
     * @parameter default-value="true"
     */
    protected boolean htmlPlayerVersionCheck;
    
    /* Internal Properties */
    
    /**
     * LW : needed for expression evaluation Note : needs at least maven 2.0.8 because of MNG-3062 The maven
     * MojoExecution needed for ExpressionEvaluation
     * 
     * @parameter expression="${mojoExecution}"
     * @required
     * @readonly
     */
    protected MojoExecution execution;
    
    protected IdeDependency globalDependency;
    
    /**
     * @parameter expression="${plugin.artifacts}"
     */
    protected List<Artifact> pluginArtifacts;
    
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
     * LW : needed for expression evaluation The maven MojoExecution needed for ExpressionEvaluation
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession sessionContext;
    
    /**
     * @component
     */
    protected VelocityComponent velocityComponent;
    
    protected LocalSdk sdk;
    
    /* End Internal Properties */

    @Override
    public void writeConfiguration( IdeDependency[] deps )
        throws MojoExecutionException
    {
    	super.writeConfiguration( deps );

        init();
        
        // Convert dependencies.
        Collection<FbIdeDependency> dependencies = getConvertedDependencies( deps );

        // Get project type
        ProjectType type = ProjectType.getProjectType( project.getPackaging(), isUseApolloConfig(), pureActionscriptProject );
        
        // Initialize new Local SDK to help with the dependency cleaning process.
        sdk = new LocalSdk( getCompilerVersion(), type );

        if ( type == ProjectType.FLEX || type == ProjectType.FLEX_LIBRARY || type == ProjectType.AIR || type == ProjectType.AIR_LIBRARY || type == ProjectType.ACTIONSCRIPT )
        {
        	dependencies = getCleanDependencies( dependencies, sdk );
            
            targetPlayer = getTargetPlayerVersion();
        	
        	writeFlexConfig( type, dependencies );
        	writeAsProperties( type, dependencies );
        }
        
        if ( type == ProjectType.FLEX )
        {
        	writeHtmlTemplate();
        }

        if ( type == ProjectType.FLEX || type == ProjectType.FLEX_LIBRARY || type == ProjectType.AIR || type == ProjectType.AIR_LIBRARY  )
        {
            writeFlexProperties();
        }
        
        if ( type == ProjectType.FLEX_LIBRARY || type == ProjectType.AIR_LIBRARY )
        {
            writeFlexLibProperties();
        }

    }

    @SuppressWarnings( "unchecked" )
    private void init()
    	throws MojoExecutionException
    {
    
        if ( services == null )
        {
            List<Resource> resources = project.getBuild().getResources();
            for ( Resource resource : resources )
            {
                File cfg = new File( resource.getDirectory(), "services-config.xml" );
                if ( cfg.exists() )
                {
                    services = cfg;
                    break;
                }
            }
        }
        
        if( definesDeclaration != null )
        {
        	cleanDefinesDeclaration();
        }

    }

    protected VelocityContext getFlexLibPropertiesContext()
    {
        VelocityContext context = new VelocityContext();
        context.put( "flexClasses", includeClasses );
        context.put( "includeFiles", getResourceEntries( includeFiles ) );
        return context;
    }
    
    protected String getFlexLibPropertiesTemplate()
    {
    	return "/templates/flexbuilder/flexLibProperties.vm";
    }
    
    private void writeFlexLibProperties()
    	throws MojoExecutionException
    {
    	runVelocity( getFlexLibPropertiesTemplate(), ".flexLibProperties", getFlexLibPropertiesContext() );
    }
    
    protected VelocityContext getFlexPropertiesContext()
    {
    	return new VelocityContext();
    }
    
    protected String getFlexPropertiesTemplate()
    {
    	return "/templates/flexbuilder/flexProperties.vm";
    }

    private void writeFlexProperties()
        throws MojoExecutionException
    {
        runVelocity( getFlexPropertiesTemplate(), ".flexProperties", getFlexPropertiesContext() );
    }

    protected Collection<FbIdeDependency> getCleanDependencies( Collection<FbIdeDependency> dependencies, LocalSdk sdk )
        throws MojoExecutionException
    {
    	// Resolves and adds all resource bundles to the dependency collection.
    	resolveResourceBundles( dependencies );
    	
    	Iterator<FbIdeDependency> iter = dependencies.iterator();
        // Loop through dependencies and perform any clean up necessary
        while( iter.hasNext() )
        {
        	FbIdeDependency dependency = iter.next();
        	
        	// Toss out any dependency that is not understood
        	if( !SWC.equals( dependency.getType() ) && !RB_SWC.equals( dependency.getType() ) )
        	{
        		iter.remove();
        		continue;
        	}
        	
        	// Configure dependency path and dependency source path
            if ( dependency.isReferencedProject() )
            {
                String projectName = dependency.getEclipseProjectName();
                // /todolist-lib/bin-debug/todolist-lib.swc
                dependency.setPath( "/" + projectName + "/bin-debug/" + projectName + ".swc" );
                dependency.setSourcePath( "/" + projectName + "/src/main/flex/" );
            }
            else
            {
                if ( useM2Repo )
                	dependency.setPath( dependency.getFile().getPath().replace( localRepository.getBasedir(), "${M2_REPO}" ) );
            }
            
        }

        return dependencies;
    }

    protected Collection<FbIdeDependency> resolveResourceBundles( Collection<FbIdeDependency> dependencies )
        throws MojoExecutionException
    {
        Collection<String> locales = getLocales();

        Collection<FbIdeDependency> extraRbs = new LinkedHashSet<FbIdeDependency>();

        Iterator<FbIdeDependency> it = dependencies.iterator();
        while( it.hasNext() )
        {
            IdeDependency dependency = it.next();
            // Ignore SWC dependencies
            if ( SWC.equals( dependency.getType() ) )
            {
                continue;
            }
            // Convert resource beacons to fully qualified resource bundles for the current locale set.
            else if ( RB_SWC.equals( dependency.getType() ) )
            {
                for ( String locale : locales )
                {
                	String scope = getDependencyScope( dependency );
                	
                    Artifact art =
                        artifactFactory.createArtifactWithClassifier( dependency.getGroupId(),
                                                                      dependency.getArtifactId(),
                                                                      dependency.getVersion(), dependency.getType(),
                                                                      locale );

                    art = MavenUtils.resolveArtifact( project, art, resolver, localRepository, remoteRepositories );

                    IdeDependency dep =
                        new IdeDependency( art.getGroupId(), art.getArtifactId(), art.getVersion(),
                                           art.getClassifier(), false, Artifact.SCOPE_TEST.equals( art.getScope() ),
                                           false, false, false, art.getFile(), art.getType(), false, null, 1,
                                           IdeUtils.getProjectName( IdeUtils.PROJECT_NAME_DEFAULT_TEMPLATE, art ) );
                    
                    // convert to FbIdeDependency to retain scope value.
                    FbIdeDependency fbdep = new FbIdeDependency( dep, scope );

                    if ( useM2Repo )
                    {
                    	fbdep.setFile( new File( fbdep.getFile().getPath().replace( localRepository.getBasedir(),
                                                                                "${M2_REPO}" ) ) );
                    }

                    extraRbs.add( fbdep );
                }
                it.remove();
            }
            // The dependency is unknown... just get rid of it.
            else
            {
                it.remove();
            }
        }
        
        dependencies.addAll( extraRbs );
        return dependencies;
    }

    private Collection<String> getLocales()
    {
        Set<String> localesList = new HashSet<String>();
        if ( locales != null )
        {
            localesList.addAll( Arrays.asList( locales ) );
        }
        if ( compiledLocales != null )
        {
            localesList.addAll( Arrays.asList( compiledLocales ) );
        }
        if ( runtimeLocales != null )
        {
            localesList.addAll( Arrays.asList( runtimeLocales ) );
        }
        if ( localesList.isEmpty() )
        {
            localesList.add( defaultLocale );
        }
        return localesList;
    }

    @SuppressWarnings( "unchecked" )
    protected VelocityContext getAsPropertiesContext( ProjectType type, Collection<FbIdeDependency> dependencies )
        throws MojoExecutionException
    {
    	File sourceDirectory = new File( project.getBuild().getSourceDirectory() );
    	
    	VelocityContext context = new VelocityContext();
        context.put( "useM2Home", useM2Repo );
        context.put( "dependencies", getNonSdkDependencies( dependencies ) );
        context.put( "sdkExcludes", sdk.getExcludes( dependencies ) );
        context.put( "sdkMods", getModifiedSdkDependencies(dependencies, sdk.getModified( dependencies ) ) );
        context.put( "mainSources", getMainSources() );
        context.put( "ideOutputFolderPath", ideOutputFolderPath );
        context.put( "targetPlayer", targetPlayer );
        context.put( "accessible", accessible );
        context.put( "strict", strict );
        context.put( "useApolloConfig", isUseApolloConfig() );
        context.put( "verifyDigests", verifyDigests );
        context.put( "showWarnings", showWarnings );
    	context.put( "flexSDK", getFlexSdkVersion() );
    	context.put( "htmlHistoryManagement", htmlHistoryManagement );
        context.put( "htmlPlayerVersionCheck", htmlPlayerVersionCheck );
        context.put( "htmlExpressInstall", htmlExpressInstall );

        StringBuilder additionalCompilerArguments = new StringBuilder();

        if ( incremental )
        {
            additionalCompilerArguments.append( " --incremental " );
        }

        if ( contextRoot != null )
        {
            additionalCompilerArguments.append( " -context-root " + contextRoot );
        }
        
        if ( configFiles != null )
        {
        	// NOTE: using just '=' causes internal build error with FlexBuilder 3 on MacOSX with Eclipse Galileo.
            //String seperator = "=";
        	String separator = "+=";
            for ( File cfg : configFiles )
            {
                additionalCompilerArguments.append( " -load-config" );
                additionalCompilerArguments.append( separator );
                additionalCompilerArguments.append( PathUtil.getRelativePath( sourceDirectory, cfg ) );
                //separator = "+=";
            }
        }

        if ( SWF.equals( packaging ) || AIR.equals( packaging ) )
        {
            File sourceFile =
                SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), this.sourceFile,
                                                      project.getGroupId(), project.getArtifactId() );

            if ( sourceFile == null )
            {
                throw new MojoExecutionException( "Could not find main application! "
                    + "(Hint: Try to create a MXML file below your source root)" );
            }

            String sourceRelativeToSourcePath =
                PathUtil.getRelativePath( sourceDirectory, sourceFile );

            context.put( "mainApplication", sourceRelativeToSourcePath );
            getAllApplications().add( 0, sourceRelativeToSourcePath );
            context.put( "applications", getAllApplications() );
            context.put( "htmlGenerate", generateHtmlWrapper );
            context.put( "cssfiles", buildCssFiles );
        }
        else if ( SWC.equals( packaging ) )
        {
            context.put( "mainApplication", project.getArtifactId() + ".as" );
            context.put( "htmlGenerate", false );
            
            // Warning: Tried to put in .flexConfig.xml but FlexBuilder complains that it doesn't know what "include-sources" is.
            if ( includeClasses == null && includeSources == null && includeNamespaces == null )
            {
            	// Changed to relative paths to eliminate issues with spaces.
            	additionalCompilerArguments.append( " -include-sources " + plain( cleanSources( getSourceRoots() ) ) );
            }
            else if ( includeSources != null )
            {
            	additionalCompilerArguments.append( " -include-sources "+ getPlainSources() );
            }
            
            // Warning: Tried to add to .flexConfig.xml but didn't work... so adding to compiler args.
            if( includeNamespaces != null && includeNamespaces.length > 0 )
            {
            	String namespaceStr = "";
            	for ( String namespace : includeNamespaces )
            	{
            		namespaceStr+=namespace+" ";
            	}
            	additionalCompilerArguments.append( " -include-namespaces " + namespaceStr );
            }
        }
        context.put( "additionalCompilerArguments", additionalCompilerArguments.toString() );
        context.put( "sources", getRelativeSources() );
        context.put( "PROJECT_FRAMEWORKS", "${PROJECT_FRAMEWORKS}" ); // flexbuilder required
        context.put( "libraryPathDefaultLinkType", getLibraryPathDefaultLinkType() ); // change flex framework linkage
        context.put( "pureActionscriptProject", pureActionscriptProject );
        context.put( "moduleFiles", moduleFiles );
        
        return context;
    }
    
    protected String getAsPropertiesTemplate()
    {
    	return "/templates/flexbuilder/actionScriptProperties.vm";
    }
    
    private void writeAsProperties( ProjectType type, Collection<FbIdeDependency> dependecies )
        throws MojoExecutionException
    {
    	runVelocity( getAsPropertiesTemplate(), ".actionScriptProperties", getAsPropertiesContext( type, dependecies ) );
    }
    
    protected Collection<FbIdeDependency> getExcludeSdkDependencies( Collection<FbIdeDependency> dependencies, List<LocalSdkEntry> excludes )
    {
    	LinkedHashSet<FbIdeDependency> excludeDeps = new LinkedHashSet<FbIdeDependency>();
    	Iterator<FbIdeDependency> iter = dependencies.iterator();
    	while( iter.hasNext() )
    	{
    		FbIdeDependency dep = iter.next();
    		if( excludes.contains( dep.getLocalSdkEntry() ) )
    		{
    			excludeDeps.add( dep );
    		}
    	}
    	return excludeDeps;
    }
    
    protected Collection<FbIdeDependency> getModifiedSdkDependencies( Collection<FbIdeDependency> dependencies, List<LocalSdkEntry> modified )
    {
    	LinkedHashSet<FbIdeDependency> modDeps = new LinkedHashSet<FbIdeDependency>();
    	Iterator<FbIdeDependency> iter = dependencies.iterator();
    	while( iter.hasNext() )
    	{
    		FbIdeDependency dep = iter.next();
    		if( modified.contains( dep.getLocalSdkEntry() ) )
    		{
    			modDeps.add( dep );
    		}
    	}
    	return modDeps;
    }
    
    protected Collection<FbIdeDependency> getNonSdkDependencies( Collection<FbIdeDependency> dependencies )
    {
    	LinkedHashSet<FbIdeDependency> nonSdkDeps = new LinkedHashSet<FbIdeDependency>();
    	Iterator<FbIdeDependency> iter = dependencies.iterator();
    	while( iter.hasNext() )
    	{
    		FbIdeDependency dep = iter.next();
    		if( !dep.getGroupId().equals( "com.adobe.flex.framework" ) )
    			nonSdkDeps.add( dep );
    	}
    	
    	return nonSdkDeps;
    }
    
    /**
     * Some compiler parameters don't work will or at all in the .actionScriptProperties.
     * Rather than clutter up the additionalCompilerAreguments more lets just write
     * stuff to a config file.
     * 
     * @param packaging
     * @param ideDependencies
     * @throws MojoExecutionException
     */
    protected VelocityContext getFlexConfigContext( ProjectType type, Collection<FbIdeDependency> ideDependencies )
    	throws MojoExecutionException
    {
    	VelocityContext context = new VelocityContext();
    	
    	context.put("namespaces", namespaces);
    	
    	if( definesDeclaration != null )
    	{
    		ExpressionEvaluator expressionEvaluator =
                new PluginParameterExpressionEvaluator( sessionContext, execution, null, null, project,
                                                        project.getProperties() );

            for ( Object definekey : definesDeclaration.keySet() )
            {
                String defineName = definekey.toString();
                String value = definesDeclaration.getProperty( defineName );
                if ( value.contains( "${" ) )
                {
                    // Fix bug in maven which doesn't always evaluate ${}
                    // constructions
                    try
                    {
                        value = (String) expressionEvaluator.evaluate( value );
                    }
                    catch ( ExpressionEvaluationException e )
                    {
                        throw new MojoExecutionException( "Expression error in " + defineName, e );
                    }
                    
                    definesDeclaration.setProperty( defineName, value );
                }
            }
            
            context.put("defines", definesDeclaration);
    	}

		context.put("metadatas", keepAs3Metadatas);

		if( SWF.equals( packaging ) || AIR.equals( packaging ) )
		{
			if ( services != null )
	            context.put( "services", services.getAbsolutePath() );
			
			if( compatibilityVersion != null )
	        	context.put( "compatibilityVersion", compatibilityVersion );
	        
	        if( keepAllTypeSelectors )
	        	context.put( "keepAllTypeSelectors", keepAllTypeSelectors );
			
			context.put( "defaultSizeWidth", defaultSizeWidth );
			context.put( "defaultSizeHeight", defaultSizeHeight );

			List<String> dependentThemes = getThemes( ideDependencies );
			context.put("themes", dependentThemes);
		}
	
		// Locales need to be available in SWC projects so merge them in.
		context.put( "locales", getLocales() );
    	
		if( configFiles == null )
    		configFiles = new ArrayList<File>();
		
		configFiles.add( new File( this.project.getBasedir() + "/.flexConfig.xml") );
    	
    	return context;
    }
    
    protected String getFlexConfigTemplate()
    {
    	return "/templates/flexbuilder/flexConfig.vm";
    }
    
    private void writeFlexConfig( ProjectType type, Collection<FbIdeDependency> dependencies )
        throws MojoExecutionException
    {
    	runVelocity( getFlexConfigTemplate(), ".flexConfig.xml", getFlexConfigContext( type, dependencies ) );
    }

    private boolean isUseApolloConfig()
    {
        return "airglobal".equals( globalDependency.getArtifactId() );
    }

    private List<String> getAllApplications()
    {
        if ( additionalApplications == null )
        {
            additionalApplications = new ArrayList<String>( 10 );
        }

        return additionalApplications;
    }

    private String getMainSources()
    {
        String mainSources =
            PathUtil.getRelativePath( project.getBasedir(), new File( project.getBuild().getSourceDirectory() ) ).replace(
                                                                                                                           '\\',
                                                                                                                           '/' );
        return mainSources;
    }

    private String getPlainSources()
    {
        Collection<String> sources = new ArrayList<String>();
        File sourceDir = new File( project.getBuild().getSourceDirectory() );
        for ( File source : includeSources )
        {
            sources.add( PathUtil.getRelativePath( sourceDir, source ) );
        }
        return plain( sources );
    }

    private String getPlainLocales()
    {
        Collection<String> locales = getLocales();
        String buf = plain( locales );
        return buf;
    }

    /**
     * Looks for the Flex framework dependency and determines result depending on specified scope.
     * 
     * @return "1" if framework is merged into code, or "3" if its a runtime shared library
     * @throws MojoExecutionException if framework dependency can not be found
     */
    private String getLibraryPathDefaultLinkType()
        throws MojoExecutionException
    {
    	LinkType type = sdk.getDefaultLinkType();
    	
        return String.valueOf( type.getId() );
    }
    

    @SuppressWarnings( "unchecked" )
    private Artifact resolveFlexFrameworkArtifact()
        throws MojoExecutionException
    {
        Set<Artifact> artifacts = project.getArtifacts();

        if ( artifacts == null )
        {
            throw new MojoExecutionException( "Could not find Flex Framework! Dependencies were not yet resolved!" );
        }

        for ( Artifact artifact : artifacts )
        {
            if ( "com.adobe.flex.framework".equals( artifact.getGroupId() )
                && ( "playerglobal".equals( artifact.getArtifactId() ) || "airglobal".equals( artifact.getArtifactId() ) )
                && "swc".equals( artifact.getType() ) )
            {
                getLog().debug(
                                "Found Flex framework artifact. Scope: [" + artifact.getScope() + "]; " + "Version: ["
                                    + artifact.getVersion() + "]" );
                return artifact;
            }
        }

        getLog().debug( "Could not find Flex Framework as a dependency! Assuming pure ActionScript project." );
        return null; // just return null if we could not find any flex dependency
    }

    protected void runVelocity( String templateName, String fileName, VelocityContext context )
        throws MojoExecutionException
    {

        Writer writer = null;
        try
        {
            Template template = velocityComponent.getEngine().getTemplate( templateName );
            writer = new FileWriter( new File( project.getBasedir(), fileName ) );
            template.merge( context, writer );
            writer.flush();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error writting " + fileName, e );
        }
        finally
        {
            if ( writer != null )
            {
                IOUtil.close( writer );
            }
        }

    }

    @Override
    public boolean setup()
        throws MojoExecutionException
    {
        Set<Artifact> depArtifacts =
            MavenUtils.getDependencyArtifacts( project, resolver, localRepository, remoteRepositories,
                                               artifactMetadataSource, artifactFactory );
        if ( pureActionscriptProject == null )
        {
            pureActionscriptProject =
                ( MavenUtils.searchFor( depArtifacts, "com.adobe.flex.framework", "framework", null, "swc", null ) == null &&
                  MavenUtils.searchFor( depArtifacts, "com.adobe.flex.framework", "flex-framework", null, "pom", null ) == null );
        }

        // include the classes
        if ( !checkNullOrEmpty( includeAsClasses ) )
        {
            try
            {
                List<String> classes = this.getClassesFromPaths();

                int length = classes.size();
                if ( includeClasses != null )
                    length += includeClasses.length;

                String[] tmp = new String[length];

                int i = 0;
                for ( String includeClass : classes )
                {
                    tmp[i] = includeClass;
                    ++i;
                }

                // merge explicit classes and scanned classes
                if ( includeClasses != null )
                    for ( int j = 0; j < includeClasses.length; j++ )
                    {
                        tmp[i] = includeClasses[j];
                        ++i;
                    }

                includeClasses = tmp;

            }
            catch ( MojoFailureException e )
            {
                e.printStackTrace();
            }
        }

        return super.setup();
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultNatures( String packaging )
    {
        super.fillDefaultNatures( packaging );

        if ( SWF.equals( packaging ) )
        {
            if ( !pureActionscriptProject )
            {
                // only add flex-app nature if this is not a pure AS project
                getProjectnatures().add( APPLICATION_NATURE );
            }
            getProjectnatures().add( ACTIONSCRIPT_NATURE );
        }

        if ( SWC.equals( packaging ) )
        {
            if ( !pureActionscriptProject )
            {
                // only add flex-lib nature if this is not a pure AS project
                getProjectnatures().add( LIBRARY_NATURE );
            }
            getProjectnatures().add( ACTIONSCRIPT_NATURE );
        }

        if ( AIR.equals( packaging ) )
        {
            getProjectnatures().add( APPLICATION_NATURE );
            getProjectnatures().add( ACTIONSCRIPT_NATURE );
            getProjectnatures().add( FLEXBUILDER_AIR_NATURE );
        }
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultClasspathContainers( String packaging )
    {
        super.fillDefaultClasspathContainers( packaging );

        if ( ( SWF.equals( packaging ) || SWC.equals( packaging ) || AIR.equals( packaging ) )
            && enableFlexBuilderBuildCommand )
        {
            getBuildcommands().add( FLEXBUILDER_BUILD_COMMAND );
        }

        if ( AIR.equals( packaging ) && enableFlexBuilderBuildCommand )
        {
            getBuildcommands().add( AIR_BUILD_COMMAND );
        }
    }
    
    /**
     * Scan the passed paths looking for Actionscript classes (namely compilation units ending in .as or .mxml as a
     * default).
     * 
     * @param includeAsClasses The paths to scan looking for classes
     * @return An array containing the name of the found classes
     * @throws MojoFailureException
     */
    @SuppressWarnings( "unchecked" )
    private List<String> getClassesFromPaths()
        throws MojoFailureException
    {

        List<String> includedFiles = new ArrayList<String>();

        for ( FileSet fileSet : includeAsClasses )
        {
            File directory = new File( fileSet.getDirectory() );
            if ( !directory.isAbsolute() )
            {
                directory = new File( this.project.getBasedir().getPath(), fileSet.getDirectory() );
            }

            if ( !directory.isDirectory() )
            {
                throw new MojoFailureException( "Source folder not found: " + PathUtil.getCanonicalPath( directory ) );
            }

            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir( directory );
            List<String> includes = fileSet.getIncludes();
            if ( ( includes != null ) && ( !includes.isEmpty() ) )
            {
                ds.setIncludes( includes.toArray( new String[includes.size()] ) );
            }
            else
            {
                ds.setIncludes( new String[] { "**/*.as", "**/*.mxml" } );
            }

            List<String> excludes = fileSet.getExcludes();
            if ( ( excludes != null ) && ( !excludes.isEmpty() ) )
            {
                ds.setExcludes( excludes.toArray( new String[excludes.size()] ) );
            }
            ds.addDefaultExcludes();
            ds.scan();

            if ( !checkNullOrEmpty( ds.getIncludedFiles() ) )
            {
                includedFiles.addAll( Arrays.asList( ds.getIncludedFiles() ) );
            }
        }

        List<String> sourceClasses = new ArrayList<String>();
        for ( String includeFile : includedFiles )
        {
            // remove extension
            includeFile = includeFile.substring( 0, includeFile.lastIndexOf( '.' ) );
            // turn paths into dots
            includeFile = includeFile.replace( '/', '.' ).replace( '\\', '.' );
            sourceClasses.add( includeFile );
        }

        return sourceClasses;
    }

    private boolean checkNullOrEmpty( Object[] array )
    {
        if ( array == null )
        {
            return true;
        }

        if ( array.length == 0 )
        {
            return false;
        }

        return false;
    }
    
    /**
     * Combines themes passed in on the themes property with themes
     * that are added as Maven dependencies with scope theme
     */
    private List<String> getThemes( Collection<FbIdeDependency> deps )
    {
    	List<String> allThemes = new ArrayList<String>();
    
    	Iterator<FbIdeDependency> it = deps.iterator();
    	while(it.hasNext())
    	{
    		FbIdeDependency dp = it.next();
    		if( dp.getScope() != null && dp.getScope().equals("theme") )
    		{
    			if( !dp.isReferencedProject() )
    			{
    				allThemes.add( dp.getPath() );
    			}
    			else
    			{
    				allThemes.add( ".." + dp.getPath() );
    			}
    		}
    	}
    	
    	Collections.reverse( allThemes );
    	
    	if(themes != null)
    	{
    		allThemes.addAll(Arrays.asList(themes));
    	}
    
    	return allThemes;
    }
    
    protected void cleanDefinesDeclaration()
    	throws MojoExecutionException
    {
    	Properties clean = new Properties();
    	
    	ExpressionEvaluator expressionEvaluator =
    		new PluginParameterExpressionEvaluator( sessionContext, execution, null, null, project,
                                                    project.getProperties() );

    	for ( Object definekey : definesDeclaration.keySet() )
    	{
    		String defineName = definekey.toString();
    		String value = definesDeclaration.getProperty( defineName );
    		if ( value.contains( "${" ) )
    		{
    			// Fix bug in maven which doesn't always evaluate ${}
    			// constructions
    			try
    			{
    				value = (String) expressionEvaluator.evaluate( value );
    			}
    			catch ( ExpressionEvaluationException e )
    			{
    				throw new MojoExecutionException( "Expression error in " + defineName, e );
    			}
    		}
	
    		// Definition values should ben quoted if necessary, so not adding additional quoting here.
    		clean.put(defineName, value);
    	}
	    
    	definesDeclaration = clean;
    }
    
    /**
     * Utility function to give IdeDependencies their missing scope value. This is needed for things like
     * theme artifacts. In addition all Flex SDK artifacts are filtered out.
     * 
     * @param dependencies
     * @return
     * @throws MojoExecutionException 
     */
    private Collection<FbIdeDependency> getConvertedDependencies( IdeDependency[] dependencies )
    	throws MojoExecutionException
    {
    	List<FbIdeDependency> fbDeps = new ArrayList<FbIdeDependency>();
    	
    	for( int i=0; i<dependencies.length; i++ )
    	{
    		IdeDependency dep = dependencies[i];
    		
    		// Include only swc and rb.swc types
    		if( SWC.equals( dep.getType() ) || RB_SWC.equals( dep.getType() ) )
    		{
    			FbIdeDependency fbDep = new FbIdeDependency( dep, getDependencyScope( dep ) );
    			
    			// Set RSL URL template if the scope is either RSL or CACHING
    			if( FlexScopes.RSL.equals( fbDep.getScope() ) || FlexScopes.CACHING.equals( fbDep.getScope() ) )
    			{
    				// NOTE artifactId, version and extension are all replaced when getRslUrl is called on the artifact (see FbIdeDependency).
    				String rslTemplate = (rslUrls != null && rslUrls.length > 0) ? rslUrls[0] : "/{contextRoot}/rsl/{artifactId}-{version}.{extension}";
    				rslTemplate = StringUtils.replace( rslTemplate , "{contextRoot}", contextRoot );
    				fbDep.setRslUrl( rslTemplate );
    				
    				String policyFileUrl = ( policyFileUrls != null && policyFileUrls.length > 0 ) ? policyFileUrls[0] : "";
    				policyFileUrl = StringUtils.replace( policyFileUrl , "{contextRoot}", contextRoot );
    				fbDep.setPolicyFileUrl( policyFileUrl );
    			}
    			
    			// Save reference to player global dependecies for later use
    			if ( ( "playerglobal".equals( fbDep.getArtifactId() ) ||
    					"airglobal".equals( fbDep.getArtifactId() ) ) && SWC.equals( fbDep.getType() ) )
    			{
    				// ignore playerglobal or airglobal that is scoped as test.
    				// these are picked up by test dependencies so need to be filtered out.
    				if( fbDep.getScope().equals( "test" ) )
    					continue;
    				
    				// Make sure global artifact is scope external.
    				fbDep.setScope( FlexScopes.EXTERNAL );
    		            	
    				globalDependency = fbDep;
    			}
    			
    			fbDeps.add( fbDep );
    		}
    	}
    	
    	return fbDeps;
    }
    
    private String getDependencyScope( IdeDependency ideDependency )
    {
    	Set<Artifact> artifacts = null;
		try
		{
			artifacts = MavenUtils.getDependencyArtifacts( project,
    	         resolver,
    	         localRepository,
    	         remoteRepositories,
    	         artifactMetadataSource,
    	         artifactFactory );
		}
		catch (MojoExecutionException e)
		{
			getLog().error("Unable to retrieve dependent artifacts.");
		}

    	Artifact artifact = null;

    	if( getLog().isDebugEnabled() )
    		getLog().debug( String.format( "Searching for artifact matching IDE Depependecy %s:%s:%s:%s",
    						ideDependency.getGroupId(),
    						ideDependency.getArtifactId(),
    						ideDependency.getVersion(),
    						ideDependency.getType() ) );

	    for( Iterator<Artifact> it = artifacts.iterator(); it.hasNext(); )
		{
	    	artifact = it.next();

	    	if( getLog().isDebugEnabled() )
	    		getLog().debug( String.format( "Checking artifact %s:%s:%s:%s",
	    						artifact.getGroupId(),
	    						artifact.getArtifactId(),
	    						artifact.getVersion(),
	    						artifact.getType() ) );

	    	// match referenced projects
	    	if( ideDependency.isReferencedProject() )
	    	{
	    		if( ideDependency.getGroupId().equals( artifact.getGroupId() ) && // match groupId
	    			ideDependency.getArtifactId().equals( artifact.getArtifactId() ) ) // match artifactId
	    		{
	    			break; // match found
	    		}
	    	}
	    	// match non referenced projects using files paths to avoid problems with SNAPSHOT version matching.
	    	else if ( artifact.getFile().equals( ideDependency.getFile() ) )
	    	{
	    		// match classifiers if needed.
	    		if( ideDependency.getClassifier() != null)
	    		{
	    			if( ideDependency.getClassifier().equals( artifact.getClassifier() ) )
	    			{
	    				break; // match found
	    			}
	    		}
	    		else
	    		{
	    			break; // match found
	    		}
	    	}

	    	// artifact did not match. null and continue loop
	    	artifact = null;
		}

	    if(artifact == null)
	    	getLog().warn("Unable to find artifact for IDE dependecy! "+ideDependency);

	    String scope = null;

	    if(artifact != null)
	    {
	    	scope = artifact.getScope();
	    }

		return scope;
    }
    
    /**
     * Utility function to put html wrapper files in the location that flex builder expects them.
     * 
     * @throws MojoExecutionException
     */
    private void writeHtmlTemplate()
		throws MojoExecutionException
	{
		if( generateHtmlWrapper )
		{
			// delete existing html template
			File outputDir = ideTemplateOutputFolder;
			if(outputDir.exists()) {
				outputDir.delete();
			}

			HtmlWrapperUtil.extractTemplate(project, templateURI, outputDir);
		}
	}
    
    /**
     * Utility function to rid sources of paths that include {locale} in them.
     * 
     * These type of paths do NOT play well with flex builder -include-sources so best to just
     * leave them out.
     * 
     * @param sources
     * @return
     */
    private Collection<String> cleanSources(Collection<String> sources)
    {
    	String[] strings = sources.toArray( new String[0] );
    	Collection<String> cleaned = new LinkedHashSet<String>();
    	File sourceDir = new File( project.getBuild().getSourceDirectory() );
    	for( int i=0; i<strings.length; i++ )
    	{
    		if( !strings[i].contains( "{locale}" ) )
    		{
    			// Convert to relative path to solve issues with spaces.
    			String relativePath = ( sourceDir.getAbsolutePath().equals( strings[i] ) ) ? "." : PathUtil.getRelativePath( sourceDir, new File( strings[i] ) );
    			cleaned.add( relativePath );
    		}
    	}
    	
    	return cleaned;
    }
    
    /**
     * Utility function to sense flex builder SDK value from framework dependencies.
     * 
     * For example:
     * 3.0.0 will import into Flexbuilder as "Flex 3"
     * 3.2.0 will import as Flex "3.2".
     * 
     * @return
     */
    protected String getFlexSdkVersion()
    {
    	String value = "default";
    	
    	Artifact compiler = MavenUtils.searchFor( pluginArtifacts, "com.adobe.flex", "compiler", null, "pom", null );
    	if( compiler != null )
    	{
    		value = "Flex ";
    		int[] version = splitVersion( compiler.getVersion() ); //compiler.getVersion().split("\\.");
    		for(int i=0; i<3; i++)
    		{
    			// take the first digit as it is
    			if(i < 1)
    			{
    				value += Integer.toString( version[i] ) + ".";
    			}
    			// if the last digit is not zero use it otherwise drop it.
    			else if( version[i] != 0 )
    			{
    				value += Integer.toString( version[i] ) + ".";
    			}
    		}
    		
    		// remove the trailing . if it exists.
    		if( value.endsWith(".") )
    			value = value.substring(0, value.length()-1);
    	}
    	
    	return value;
 
    }
    
    /**
     * Builds a collection of resource entries based of an array of files.
     * Each ResourceEntry has a destination and a source. The source is the
     * absolute path of the file include and the destination is a relative path
     * starting at the source path root. The destination path is important
     * because this is the path to where it will end up in the compiled SWC
     *
     * An example would be:
     * Source path = ${basedir}/src/main/resources/org/proj/myfile.txt
     * Destination path = org/proj/myfile.txt
     * @param includeFiles
     * @return
     */
     private Collection<ResourceEntry> getResourceEntries( File[] includeFiles )
     {
     	Collection<ResourceEntry> entries = new ArrayList<ResourceEntry>();
         
     	Collection<String> sourceRoots = getSourceRoots();
         
     	if( includeFiles != null )
     	{
     		for( int i=0; i<includeFiles.length; i++ )
     		{
     			File includeFile = includeFiles[i];
     			String sourcePath = includeFile.getAbsolutePath();
      
     			// Strip source roots from destination and source paths.
     			String destPath = "";
     			for( String sourceRoot : sourceRoots )
     			{
     				if( sourcePath.contains( sourceRoot) )
     				{
     					int srl = sourceRoot.length();
     					destPath = sourcePath.substring( srl+1 );
     					sourcePath = destPath;
     				}
     			}
      
     			// If the source path is not relative to any source roots
     			// then the destination path will use the full source path.
     			if(destPath.length() < 1)
     			{
     				destPath = sourcePath;
     			}
      
     			entries.add( new ResourceEntry( destPath, sourcePath ) );
     		}
     	}
         
     	return entries;
     }

     protected String getTargetPlayerVersion()
         throws MojoExecutionException
     {
    	 String version = null;
    	 
         IdeDependency globalArtifact = getGlobalArtifact();
         if ( globalArtifact.getArtifactId().equals( "airglobal" ) )
         {
             // not sure what to do here
             getLog().warn( "Target player not set, not sure how to behave on air projects" );
             return version;
         }

         String globalVersion = globalArtifact.getClassifier();
         int[] playerGlobalVersion;
         if ( globalVersion == null )
         {
             // Older playerglobal artifacts had the version appended to the artifact version.
        	 // Example: 9-3.2.0.3958
             if( globalArtifact.getVersion().contains( "-" ) )
             {
            	 globalVersion = globalArtifact.getVersion().split( "-" )[0];
            	 if( globalVersion == null )
            	 {
            		 getLog().warn( "Player global doesn't cointain classifier" );
            		 return version;
            	 }
            	 else
            	 {
            		 playerGlobalVersion = splitVersion( globalVersion );
            	 }
             }
             else
             {
            	 getLog().warn( "Player global doesn't cointain classifier" );
            	 return version;
             }
         }
         else
         {
             playerGlobalVersion = splitVersion( globalVersion );
         }

         if ( targetPlayer != null )
         {
        	 version = targetPlayer;
         }
         else
         {
        	 // target player version not specified so create it from the maven artifact
        	 int[] tmpVersion = splitVersion( globalVersion, 3 );
        	 StringBuffer sb = new StringBuffer();
        	 for( int i=0; i<tmpVersion.length; i++ )
        	 {
        		 if( i > 0 )
        			 sb.append( "." );
        		 
        		 sb.append( tmpVersion[i] );
        	 }
        	 version = sb.toString();
         }

         int[] versions = splitVersion( version, 3 );
         if ( versions[0] < 9 )
         {
             throw new MojoExecutionException( "Invalid target player version " + targetPlayer );
         }

         if ( !isMinVersionOK( playerGlobalVersion, versions ) )
         {
             throw new MojoExecutionException(
                                               "TargetPlayer and playerglobal dependency version doesn't match! Target player: "
                                                   + targetPlayer + ", player global: " + globalVersion );
         }
         
         return version;
     }
     
     protected IdeDependency getGlobalArtifact()
     	throws MojoExecutionException
	 {
	     if( globalDependency != null )
	     {
	         return globalDependency;
	     }

	     throw new MojoExecutionException( "Player/Air Global dependency not found." );
	 }
    
     
     public String getCompilerVersion()
     {
         Artifact compiler = MavenUtils.searchFor( pluginArtifacts, "com.adobe.flex", "compiler", null, "pom", null );
         return compiler.getVersion();
     }
     
     
}
