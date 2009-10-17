/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.compiler;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.sonatype.flexmojos.common.FlexExtension.SWF;
import static org.sonatype.flexmojos.common.FlexExtension.SWZ;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jvnet.animal_sniffer.IgnoreJRERequirement;
import org.sonatype.flexmojos.common.ApplicationDependencySorter;
import org.sonatype.flexmojos.common.ApplicationDependencySorter.StaticRSLScope;
import org.sonatype.flexmojos.compatibilitykit.FlexCompatibility;
import org.sonatype.flexmojos.truster.FlashPlayerTruster;
import org.sonatype.flexmojos.truster.TrustException;
import org.sonatype.flexmojos.utilities.MavenUtils;
import org.sonatype.flexmojos.utilities.SourceFileResolver;

import flex2.tools.oem.Application;
import flex2.tools.oem.Configuration;
import flex2.tools.oem.internal.OEMConfiguration;

/**
 * <p>
 * Goal which compiles the Flex sources into an application for either Flex or AIR depending on the package type.
 * </p>
 * <p>
 * The Flex Compiler plugin compiles all ActionScript sources. It can compile the source into 'swf' files. The plugin
 * supports 'swf' packaging.
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 1.0
 * @goal compile-swf
 * @requiresDependencyResolution compile
 * @phase compile
 */
public class ApplicationMojo
    extends AbstractFlexCompilerMojo<Application, ApplicationDependencySorter>
{
    public static final String[] DEFAULT_RSL_URLS =
        new String[] { "/{contextRoot}/rsl/{artifactId}-{version}.{extension}" };

    /**
     * The file to be compiled. The path must be relative with source folder
     * 
     * @parameter
     */
    protected String sourceFile;

    /**
     * The list of modules files to be compiled. The path must be relative with source folder.<BR>
     * Usage:
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

    private List<File> modules;

    /**
     * When true, tells flexmojos to use link reports/load externs on modules compilation
     * 
     * @parameter default-value="true" expression="${loadExternsOnModules}"
     */
    private boolean loadExternsOnModules;

    /**
     * The file to be compiled
     */
    protected File source;

    /**
     * When true, flexmojos will register register every compiled SWF files as trusted. These SWF files are assigned to
     * the local-trusted sandbox. They can interact with any other SWF files, and they can load data from anywhere,
     * remote or local. On false nothing is done, so if the file is already trusted it will still as it is.
     * 
     * @parameter default-value="true" expression="${updateSecuritySandbox}"
     */
    private boolean updateSecuritySandbox;

    /**
     * Turn on generation of debuggable SWFs. False by default for mxmlc, but true by default for compc.
     * 
     * @parameter default-value="false"
     */
    private boolean debug;

    /**
     * Default locale for libraries. This is useful to non localized applications, just to define swc.rb locale
     * 
     * @parameter default-value="en_US"
     */
    private String defaultLocale;

    /**
     * @parameter default-value="true"
     */
    private boolean useDefaultLocale;

    /**
     * Default scope for SWC - merged or external
     * 
     * @parameter default-value="merged"
     */
    private String defaultScope;

    /**
     * Default scope for RSL/Caching - default (specified scope remain unchanged), internal, merged or external
     * 
     * @parameter default-value="default" expression="${rslScope}"
     */
    private String rslScope;

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
    private String[] themes;

    /**
     * Determines whether to compile against libraries statically or use RSLs. Set this option to true to ignore the
     * RSLs specified by the <code>rslUrls</code>. Set this option to false to use the RSLs.
     * <p>
     * Add the static-link-runtime-shared-libraries=true option; this ensures that you are not using the framework RSL
     * when compiling the application, regardless of the settings in your configuration files. Instead, you are
     * compiling the framework classes into your SWF file.
     * </p>
     * http://livedocs.adobe.com/flex/3/html/help.html?content=rsl_09.html
     * 
     * @parameter default-value="false"
     * @deprecated Use rslScope = merged
     */
    private boolean staticLinkRuntimeSharedLibraries;

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
    private String[] rslUrls;

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
     * @component
     */
    private FlashPlayerTruster truster;

    @Override
    protected void fixConfigReport( FlexConfigBuilder configBuilder )
    {
        super.fixConfigReport( configBuilder );

        configBuilder.addList( new String[] { source.getAbsolutePath() }, "file-specs", "path-element" );
    }

    protected void setUpDependencySorter()
        throws MojoExecutionException
    {
        dependencySorter = new ApplicationDependencySorter();
        StaticRSLScope rslScopeEnum = StaticRSLScope.valueOf( rslScope.toUpperCase() );
        dependencySorter.sort( project, defaultScope, rslScopeEnum );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        setUpDependencySorter();

        File sourceDirectory = new File( build.getSourceDirectory() );
        if ( !sourceDirectory.exists() )
        {
            throw new MojoExecutionException( "Unable to found sourceDirectory: " + sourceDirectory );
        }

        if ( source == null )
        {
            if ( sourceFile == null )
            {
                getLog().warn( "Source file was not defined, flexmojos will guess one." );
            }
            source =
                SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), sourceFile,
                                                      project.getGroupId(), project.getArtifactId() );
        }

        if ( source == null )
        {
            throw new MojoExecutionException( "Source file not expecified and no default found!" );
        }
        if ( !source.exists() )
        {
            throw new MojoFailureException( "Unable to find " + sourceFile );
        }

        // need to initialize builder before go super
        try
        {
            builder = new Application( source );
        }
        catch ( FileNotFoundException e )
        {
            throw new MojoFailureException( "Unable to find " + source );
        }

        if ( moduleFiles != null )
        {
            modules = new ArrayList<File>();
            for ( String modulePath : moduleFiles )
            {
                File module = new File( sourceDirectory, modulePath );
                if ( !module.exists() )
                {
                    throw new MojoExecutionException( "Module " + module + " not found." );
                }
                modules.add( module );
            }

            if ( loadExternsOnModules )
            {
                super.linkReport = true;
            }
        }

        if ( rslUrls == null )
        {
            rslUrls = DEFAULT_RSL_URLS;
        }

        if ( policyFileUrls == null )
        {
            policyFileUrls = new String[rslUrls.length];
        }
        for ( int i = 0; i < policyFileUrls.length; i++ )
        {
            policyFileUrls[i] = policyFileUrls[i] == null ? "" : policyFileUrls[i];
        }

        super.setUp();

        builder.setOutput( getOutput() );
    }

    @Override
    protected void configureViaCommandLine( List<String> commandLineArguments )
    {
        super.configureViaCommandLine( commandLineArguments );

        commandLineArguments.add( "-static-link-runtime-shared-libraries="
            + ( rslScope.equals( "default" ) ? "false" : "true" ) );
    }

    @Override
    protected void resolveDependencies()
        throws MojoExecutionException, MojoFailureException
    {
        super.resolveDependencies();

        resolveRuntimeLibraries();
        configuration.setTheme( getThemes() );
    }

    protected File[] getThemes()
        throws MojoExecutionException, MojoFailureException
    {
        List<File> themeFiles = new ArrayList<File>();
        if ( this.themes != null )
        {
            for ( String theme : themes )
            {
                File themeFile = MavenUtils.resolveResourceFile( project, theme );
                themeFiles.add( themeFile );
            }
        }

        themeFiles.addAll( Arrays.asList( dependencySorter.getThemes() ) );

        if ( themeFiles.isEmpty() )
        {
            return null;
        }

        return themeFiles.toArray( new File[themeFiles.size()] );
    }

    protected void configureIncludeResourceBundles( OEMConfiguration oemConfig )
    {
        if ( includeResourceBundles != null )
        {
            oemConfig.setIncludeResourceBundles( includeResourceBundles );
        }
    }

    @Override
    protected void tearDown()
        throws MojoExecutionException, MojoFailureException
    {
        super.tearDown();

        updateSecuritySandbox( getOutput() );

        if ( modules != null )
        {
            compileModules();
        }
    }

    protected void compileModules()
        throws MojoFailureException, MojoExecutionException
    {

        configuration.addExternalLibraryPath( dependencySorter.getRSLLibraries() );
        configuration.addExternalLibraryPath( dependencySorter.getCachingLibraries() );
        configuration.setRuntimeSharedLibraryPath( "", new String[] { "" }, new String[] { "" } );

        if ( loadExternsOnModules )
        {
            configuration.addExterns( new File[] { linkReportFile } );
        }

        for ( File module : modules )
        {
            getLog().info( "Compiling module " + module );
            String moduleName = module.getName();
            moduleName = moduleName.substring( 0, moduleName.lastIndexOf( '.' ) );

            Application moduleBuilder;
            try
            {
                moduleBuilder = new Application( module );
            }
            catch ( FileNotFoundException e )
            {
                throw new MojoFailureException( "Unable to find " + module, e );
            }

            setMavenPathResolver( moduleBuilder );
            moduleBuilder.setConfiguration( configuration );
            moduleBuilder.setLogger( new CompileLogger( getLog() ) );
            File outputModule =
                new File( build.getDirectory(), build.getFinalName() + "-" + moduleName + "." + project.getPackaging() );
            updateSecuritySandbox( outputModule );

            moduleBuilder.setOutput( outputModule );

            build( moduleBuilder, false );

            projectHelper.attachArtifact( project, SWF, moduleName, outputModule );

        }
    }

    @FlexCompatibility( minVersion = "3", maxVersion = "3.1" )
    @IgnoreJRERequirement
    protected void writeResourceBundleFlex30( String[] bundles, String locale, File localePath )
        throws MojoExecutionException
    {
        // Dont break this method in parts, is a work around

        File output = getRuntimeLocaleOutputFile( locale, SWF );

        /*
         * mxmlc -locale=en_US -source-path=locale/{locale} -include-resource-bundles
         * =FlightReservation2,SharedResources,collections ,containers,controls,core,effects,formatters,skins,styles
         * -output=src/Resources_en_US.swf
         */

        String bundlesString = Arrays.toString( bundles ) //
        .replace( "[", "" ) // remove start [
        .replace( "]", "" ) // remove end ]
        .replace( ", ", "," ); // remove spaces

        ArrayList<File> external = new ArrayList<File>();
        ArrayList<File> internal = new ArrayList<File>();
        ArrayList<File> merged = new ArrayList<File>();

        Collections.addAll( external, dependencySorter.getExternalLibraries() );
        Collections.addAll( external, dependencySorter.getRSLLibraries() );
        Collections.addAll( external, dependencySorter.getCachingLibraries() );

        Collections.addAll( internal, dependencySorter.getInternalLibraries() );

        Collections.addAll( merged, dependencySorter.getMergedLibraries() );
        Collections.addAll( merged, getResourcesBundles( locale ) );

        Set<String> args = new HashSet<String>();
        // args.addAll(Arrays.asList(configs));
        args.add( "-locale=" + locale );
        if ( localePath != null )
        {
            args.add( "-source-path=" + localePath.getAbsolutePath() );
        }
        args.add( "-include-resource-bundles=" + bundlesString );
        args.add( "-output=" + output.getAbsolutePath() );
        args.add( "-compiler.fonts.local-fonts-snapshot=" + getFontsSnapshot().getAbsolutePath() );
        args.add( "-load-config=" + ( configFile == null ? "" : configFile.getAbsolutePath() ) );
        args.add( "-external-library-path=" + toString( external ) );
        args.add( "-include-libraries=" + toString( internal ) );
        args.add( "-library-path=" + toString( merged ) );

        getLog().debug( "writeResourceBundle calling mxmlc with args: " + args.toString() );
        forkMxmlc( args );
        runMxmlc( args );

        projectHelper.attachArtifact( project, SWF, locale, output );
    }

    @FlexCompatibility( maxVersion = "2" )
    @IgnoreJRERequirement
    private void forkMxmlc( Set<String> args )
        throws MojoExecutionException
    {
        throw new MojoExecutionException( "Not implemented yet" );
    }

    @FlexCompatibility( minVersion = "3", maxVersion = "3.1" )
    @IgnoreJRERequirement
    private void runMxmlc( Set<String> args )
    {
        // Just a work around
        // TODO https://bugs.adobe.com/jira/browse/SDK-15139
        flex2.tools.Compiler.mxmlc( args.toArray( new String[args.size()] ) );
    }

    private String toString( List<File> libs )
    {
        StringBuilder sb = new StringBuilder();
        for ( File lib : libs )
        {
            if ( sb.length() != 0 )
            {
                sb.append( ',' );
            }

            sb.append( lib.getAbsolutePath() );
        }
        return sb.toString();
    }

    @Override
    protected void writeResourceBundle( String[] bundlesNames, String locale, File localePath )
        throws MojoExecutionException
    {
        writeResourceBundleFlex30( bundlesNames, locale, localePath );
        writeResourceBundleFlex32( bundlesNames, locale, localePath );
    }

    @FlexCompatibility( minVersion = "3.2" )
    @IgnoreJRERequirement
    protected void writeResourceBundleFlex32( String[] bundles, String locale, File localePath )
        throws MojoExecutionException
    {
        Application rbBuilder = new Application();
        File output = getRuntimeLocaleOutputFile( locale, SWF );

        rbBuilder.setLogger( builder.getLogger() );
        rbBuilder.setOutput( output );
        rbBuilder.setConfiguration( getResourceBundleConfiguration( bundles, locale, localePath ) );

        build( rbBuilder, true );

        if ( configurationReport )
        {
            try
            {
                FlexConfigBuilder configBuilder = new FlexConfigBuilder( rbBuilder );
                configBuilder.addOutput( output );
                configBuilder.write( new File( output.getPath().replace( "." + SWF, "-config-report.xml" ) ) );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "An error has ocurried while recording config-report", e );
            }
        }

        projectHelper.attachArtifact( project, SWF, locale, output );
    }

    @Override
    protected boolean isDebug()
    {
        return this.debug;
    }

    @Override
    protected boolean isApplication()
    {
        return true;
    }

    @Override
    protected String getDefaultLocale()
    {
        if ( useDefaultLocale )
        {
            return this.defaultLocale;
        }
        return null;
    }

    protected Configuration getResourceBundleConfiguration( String[] bundles, String locale, File localePath )
        throws MojoExecutionException
    {
        if ( configuration instanceof OEMConfiguration )
        {
            OEMConfiguration oemConfiguration = (OEMConfiguration) configuration;
            if ( bundles != null )
            {
                oemConfiguration.setIncludeResourceBundles( bundles );
            }

            oemConfiguration.setConfiguration( new String[] { "-static-link-runtime-shared-libraries=true",
                "-load-config=" } );
        }

        setLocales( locale );
        if ( localePath != null )
        {
            configuration.setSourcePath( new File[] { localePath } );
        }

        configuration.includeLibraries( null );

        configuration.addExternalLibraryPath( dependencySorter.getInternalLibraries() );
        configuration.addExternalLibraryPath( dependencySorter.getRSLLibraries() );
        configuration.addExternalLibraryPath( dependencySorter.getCachingLibraries() );

        configuration.setLibraryPath( getResourcesBundles( locale ) );
        configuration.addLibraryPath( dependencySorter.getMergedLibraries() );

        configuration.setServiceConfiguration( null );

        return configuration;
    }

    /**
     * Resolves all runtime libraries, that includes RSL and framework CACHING
     * 
     * @throws MojoExecutionException
     */
    private void resolveRuntimeLibraries()
        throws MojoExecutionException
    {
        List<Artifact> rsls = dependencySorter.getRSLAndCachingArtifacts();
        rslsSort( rsls );

        for ( Artifact artifact : rsls )
        {
            addRuntimeLibrary( artifact );
        }
    }

    @SuppressWarnings( { "deprecation" } )
    protected void addRuntimeLibrary( Artifact artifact )
    {
        String scope = artifact.getScope();
        File artifactFile = artifact.getFile();
        String artifactPath = artifactFile.getAbsolutePath();
        String extension;
        if ( CACHING.equals( scope ) )
        {
            extension = SWZ;
        }
        else
        {
            extension = SWF;
        }
        String[] rslUrls = getRslUrls( artifact, extension );
        String[] rslPolicyFileUrls = getRslPolicyFileUrls( artifact );
        configuration.addRuntimeSharedLibraryPath( artifactPath, rslUrls, rslPolicyFileUrls );
    }

    public void rslsSort( List<Artifact> rslArtifacts )
        throws MojoExecutionException
    {
        Map<Artifact, List<Artifact>> dependencies = getDependencies( rslArtifacts );

        List<Artifact> ordered = new ArrayList<Artifact>();
        for ( Artifact a : rslArtifacts )
        {
            if ( dependencies.get( a ) == null || dependencies.get( a ).isEmpty() )
            {
                ordered.add( a );
            }
        }
        rslArtifacts.removeAll( ordered );

        while ( !rslArtifacts.isEmpty() )
        {
            int original = rslArtifacts.size();
            for ( Artifact a : rslArtifacts )
            {
                List<Artifact> deps = dependencies.get( a );
                if ( ordered.containsAll( deps ) )
                {
                    ordered.add( a );
                }
            }
            rslArtifacts.removeAll( ordered );
            if ( original == rslArtifacts.size() )
            {
                throw new MojoExecutionException( "Unable to resolve " + rslArtifacts );
            }
        }

        rslArtifacts.addAll( ordered );
    }

    /**
     * Get RslUrls
     * 
     * @param artifact
     * @param extension
     * @return Array of url's
     */
    private String[] getRslUrls( Artifact artifact, String extension )
    {
        String[] rsls = new String[rslUrls.length];
        for ( int i = 0; i < rslUrls.length; i++ )
        {
            String rsl = rslUrls[i];
            if ( contextRoot == null || "".equals( contextRoot ) )
            {
                rsl = rsl.replace( "/{contextRoot}/", "" );
            }
            else
            {
                rsl = rsl.replace( "{contextRoot}", contextRoot );
            }
            rsl = MavenUtils.getRslUrl( rsl, artifact, extension );
            rsls[i] = rsl;
        }
        return rsls;
    }

    /**
     * Gets RslPolicyFileUrls for given artifact
     * 
     * @param artifact
     * @return Array of urls
     */
    private String[] getRslPolicyFileUrls( Artifact artifact )
    {
        String[] domains = new String[policyFileUrls.length];
        for ( int i = 0; i < policyFileUrls.length; i++ )
        {
            String domain = policyFileUrls[i];
            if ( contextRoot != null )
            {
                domain = domain.replace( "{contextRoot}", contextRoot );
            }
            domain = MavenUtils.replaceArtifactCoordinatesTokens( domain, artifact );
            domains[i] = domain;
        }
        return domains;
    }

    @SuppressWarnings( "unchecked" )
    private Map<Artifact, List<Artifact>> getDependencies( List<Artifact> rslArtifacts )
        throws MojoExecutionException
    {
        Map<Artifact, List<Artifact>> dependencies = new HashMap<Artifact, List<Artifact>>();

        for ( Artifact pomArtifact : rslArtifacts )
        {
            try
            {
                MavenProject pomProject =
                    mavenProjectBuilder.buildFromRepository( pomArtifact, remoteRepositories, localRepository );
                Set pomArtifacts = pomProject.createArtifacts( artifactFactory, null, null );
                ArtifactResolutionResult arr =
                    resolver.resolveTransitively( pomArtifacts, pomArtifact, remoteRepositories, localRepository,
                                                  artifactMetadataSource );
                List<Artifact> artifactDependencies = new ArrayList<Artifact>( arr.getArtifacts() );
                artifactDependencies = removeNonRSLDependencies( rslArtifacts, artifactDependencies );
                dependencies.put( pomArtifact, artifactDependencies );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
        return dependencies;
    }

    private List<Artifact> removeNonRSLDependencies( List<Artifact> rslArtifacts, List<Artifact> artifactDependencies )
    {
        List<Artifact> cleanArtifacts = new ArrayList<Artifact>();
        artifacts: for ( Artifact artifact : artifactDependencies )
        {
            for ( Artifact rslArtifact : rslArtifacts )
            {
                if ( artifact.getGroupId().equals( rslArtifact.getGroupId() )
                    && artifact.getArtifactId().equals( rslArtifact.getArtifactId() )
                    && artifact.getType().equals( rslArtifact.getType() ) )
                {
                    cleanArtifacts.add( rslArtifact );
                    continue artifacts;
                }
            }
        }
        return cleanArtifacts;
    }

    protected void updateSecuritySandbox( File output )
        throws MojoExecutionException
    {
        if ( updateSecuritySandbox )
        {
            try
            {
                truster.updateSecuritySandbox( getOutput() );
            }
            catch ( TrustException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
    }
}
