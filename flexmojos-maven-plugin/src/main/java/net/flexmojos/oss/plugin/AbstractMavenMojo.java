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
package net.flexmojos.oss.plugin;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.selectFirst;
import static com.marvinformatics.kiss.matchers.maven.artifact.ArtifactMatchers.artifactId;
import static com.marvinformatics.kiss.matchers.maven.artifact.ArtifactMatchers.classifier;
import static com.marvinformatics.kiss.matchers.maven.artifact.ArtifactMatchers.groupId;
import static com.marvinformatics.kiss.matchers.maven.artifact.ArtifactMatchers.type;
import static net.flexmojos.oss.plugin.common.FlexExtension.AS;
import static net.flexmojos.oss.plugin.common.FlexExtension.MXML;
import static net.flexmojos.oss.plugin.common.FlexExtension.SWC;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.flexmojos.oss.compatibilitykit.VersionUtils;
import net.flexmojos.oss.compiler.command.Result;
import net.flexmojos.oss.plugin.common.flexbridge.MavenLogger;
import net.flexmojos.oss.plugin.common.flexbridge.MavenPathResolver;
import net.flexmojos.oss.plugin.compiler.attributes.MavenRuntimeException;
import net.flexmojos.oss.plugin.compiler.lazyload.Cacheable;
import net.flexmojos.oss.plugin.compiler.lazyload.NotCacheable;
import net.flexmojos.oss.plugin.utilities.MavenUtils;
import net.flexmojos.oss.util.PathUtil;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.PatternSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.ContextEnabled;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.hamcrest.Matcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import flex2.compiler.Logger;
import flex2.compiler.common.SinglePathResolver;
import flex2.tools.oem.internal.OEMLogAdapter;

public abstract class AbstractMavenMojo
    implements Mojo, Cacheable, ContextEnabled
{

    public static final String AIR_GLOBAL = "airglobal";

    public static final String COMPILER_GROUP_ID = "com.adobe.flex.compiler";

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    public static final String[] DEFAULT_RSL_URLS =
        new String[] { "/{contextRoot}/rsl/{artifactId}-{version}.{extension}" };

    public static final String DEFAULT_RUNTIME_LOCALE_OUTPUT_PATH =
        "/{contextRoot}/locales/{artifactId}-{version}-{locale}.{extension}";

    public static final String FRAMEWORK_GROUP_ID = "com.adobe.flex.framework";

    public static final String PLAYER_GLOBAL = "playerglobal";

    public static final Answer<Object> RETURNS_NULL = new Answer<Object>()
    {
        @Override
        public Object answer( InvocationOnMock invocation )
            throws Throwable
        {
            return null;
        }
    };

    public static final String TARGET_DIRECTORY = "getTargetDirectory";

    /**
     * @component
     * @readonly
     */
    protected ArchiverManager archiverManager;

    /**
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    protected Map<String, Object> cache = new LinkedHashMap<String, Object>();

    /**
     * The maven configuration directory
     * 
     * @parameter expression="${basedir}/src/main/config"
     * @required
     * @readonly
     */
    protected File configDirectory;

    /**
     * When false (faster) Flexmojos will compiler modules and resource bundles using multiple threads (One per SWF). If
     * true, Thread.join() will be invoked to make the execution synchronous (sequential).
     * 
     * @parameter expression="${flex.fullSynchronization}" default-value="false"
     */
    protected boolean fullSynchronization;

    /**
     * Adobe AIR version
     *
     * @parameter expression="${flex.airVersion}"
     */
    private String airVersion;

    protected final Matcher<? super Artifact> GLOBAL_MATCHER = initGlobalMatcher();

    /**
     * Local repository to be used by the plugin to resolve dependencies.
     * 
     * @parameter expression="${localRepository}"
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * Maven logger
     * 
     * @readonly
     */
    Log log;

    /**
     * @parameter expression="${project.build.outputDirectory}"
     * @readonly
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression="${project.packaging}"
     * @required
     * @readonly
     */
    protected String packaging;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @readonly
     */
    protected List<Artifact> pluginArtifacts;

    private Map<Object, Object> pluginContext;

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
     * @readonly
     * @required
     */
    protected MavenProjectHelper projectHelper;

    /**
     * Quick compile mode. When true, Flexmojos will check if the latest artifact available at maven repository for this
     * project is newer then sources. If so, wont recompile.
     * 
     * @parameter default-value="false" expression="${flexmojos.quick}"
     */
    protected boolean quick;

    /**
     * List of remote repositories to be used by the plugin to resolve dependencies.
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     */
    protected List<ArtifactRepository> remoteRepositories;

    /**
     * @component
     * @readonly
     */
    protected RepositorySystem repositorySystem;

    /**
     * The maven resources
     * 
     * @parameter expression="${project.build.resources}"
     * @required
     * @readonly
     */
    protected List<Resource> resources;

    /**
     * The Maven Session Object
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession session;

    /**
     * Skips flexmojos goal execution
     * 
     * @parameter expression="${flexmojos.skip}"
     */
    protected boolean skip;

    /**
     * @parameter expression="${project.build.directory}"
     * @readonly
     * @required
     */
    protected File targetDirectory;

    /**
     * Set this configuration parameter when your AIR unit tests
     * require a special configuration in the app descriptor in order to run.
     * e.g. requiring a specific renderMode setting
     *
     * You may want to take a look under resources at
     * the file /templates/test/air-descriptor-template.xml
     * for a good baseline example to modify.
     *
     * @parameter
     */
    private File testAirDescriptorTemplate;

    public AbstractMavenMojo()
    {
        super();
    }

    protected List<FileSet> as3ClassesFileSet( File... files )
    {
        if ( files == null )
        {
            return null;
        }

        List<FileSet> sets = new ArrayList<FileSet>();
        for ( File file : files )
        {
            FileSet fs = new FileSet();
            fs.setDirectory( PathUtil.path( file ) );
            fs.addInclude( "**/*.as" );
            fs.addInclude( "**/*.mxml" );
            sets.add( fs );
        }

        return sets;
    }

    protected void checkResult( Result result )
        throws MojoFailureException, MojoExecutionException
    {
        int exitCode;
        try
        {
            exitCode = result.getExitCode();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        if ( exitCode != 0 )
        {
            throw new MojoFailureException( "Got " + exitCode + " errors building project, check logs" );
        }
    }

    protected File createSwfDescriptor( File swf )
    {
        Reader reader = null;
        FileWriter writer = null;
        try
        {
            if (testAirDescriptorTemplate != null)
            {
                reader = new FileReader(testAirDescriptorTemplate);
            }
            else
            {
                reader =
                    new InputStreamReader( getClass().getResourceAsStream( "/templates/test/air-descriptor-template.xml" ) );
            }

            Map<String, String> variables = new LinkedHashMap<String, String>();
            variables.put( "id", swf.getName().replaceAll( "[^A-Za-z0-9]", "" ) );
            variables.put( "swf", swf.getName() );
            variables.put( "air-version", getAirTarget() );

            InterpolationFilterReader filterReader = new InterpolationFilterReader( reader, variables );

            File destFile = new File( swf.getParentFile(), FilenameUtils.getBaseName( swf.getName() ) + ".xml" );
            writer = new FileWriter( destFile );

            IOUtil.copy( filterReader, writer );

            return destFile;
        }
        catch ( IOException e )
        {
            throw new MavenRuntimeException( "Fail to create test air descriptor", e );
        }
        finally
        {
            IOUtil.close( reader );
            IOUtil.close( writer );
        }
    }

    protected List<String> filterClasses( List<FileSet> classesPattern, File[] directories )
    {
        directories = PathUtil.existingFiles( directories );

        Set<String> includedFiles = new LinkedHashSet<String>();
        for ( FileSet pattern : classesPattern )
        {
            pattern.setIncludes( toFilePattern( pattern.getIncludes() ) );
            pattern.setExcludes( toFilePattern( pattern.getExcludes() ) );

            if ( pattern.getDirectory() == null )
            {
                for ( File dir : directories )
                {
                    includedFiles.addAll( Arrays.asList( scan( pattern, dir ).getIncludedFiles() ) );
                }
            }
            else
            {
                File dir = PathUtil.file( pattern.getDirectory(), getBasedir() );
                if ( !ArrayUtils.contains( directories, dir ) )
                {
                    throw new IllegalArgumentException( "Pattern does point to an invalid source directory: "
                        + dir.getAbsolutePath() );
                }

                includedFiles.addAll( Arrays.asList( scan( pattern, dir ).getIncludedFiles() ) );
            }
        }

        List<String> classes = new ArrayList<String>();
        for ( String filename : includedFiles )
        {
            String classname = toClass( filename );
            classes.add( classname );
        }

        return classes;
    }

    protected Collection<File> filterFiles( List<FileSet> patterns, List<File> directories )
    {
        directories = PathUtil.existingFilesList( directories );

        Set<File> includedFiles = new LinkedHashSet<File>();
        for ( FileSet pattern : patterns )
        {
            if ( pattern.getDirectory() == null )
            {
                for ( File dir : directories )
                {
                    DirectoryScanner scan = scan( pattern, dir );
                    includedFiles.addAll( PathUtil.files( scan.getIncludedFiles(), dir ) );
                }
            }
            else
            {
                File dir = PathUtil.file( pattern.getDirectory(), getBasedir() );
                if ( !directories.contains( dir ) )
                {
                    throw new IllegalArgumentException( "Pattern does point to an invalid directory: "
                        + dir.getAbsolutePath() );
                }

                includedFiles.addAll( PathUtil.files( scan( pattern, dir ).getIncludedFiles(), dir ) );
            }
        }

        return includedFiles;
    }

    public String getAirTarget()
    {
        if (airVersion == null)
        {
            int[] version = VersionUtils.splitVersion( getCompilerVersion() );
            if ( VersionUtils.isMinVersionOK( version, new int[] { 4, 6 } ) )
            {
                return "3.1";
            }
            if ( VersionUtils.isMinVersionOK( version, new int[] { 4, 5, 0, 19787 } ) )
            {
                return "2.6";
            }
            if ( VersionUtils.isMinVersionOK( version, new int[] { 4, 5, 0 } ) )
            {
                return "2.5";
            }
            if ( VersionUtils.isMinVersionOK( version, new int[] { 4, 1, 0 } ) )
            {
                return "2.0";
            }
            if ( VersionUtils.isMinVersionOK( version, new int[] { 3, 2, 0 } ) )
            {
                return "1.5";
            }

            return "1.0";
        }
        else return airVersion;
    }

    protected File getBasedir()
    {
        return basedir;
    }

    @Override
    @NotCacheable
    public Map<String, Object> getCache()
    {
        return cache;
    }

    public String getCompilerVersion()
    {
        Artifact compiler = MavenUtils.searchFor( pluginArtifacts, "com.adobe.flex", "compiler", null, "pom", null );
        return compiler.getVersion();
    }

    public Set<Artifact> getDependencies()
    {
        return Collections.unmodifiableSet( project.getArtifacts() );
    }

    protected Set<Artifact> getDependencies( Matcher<? super Artifact>... matchers )
    {
        Set<Artifact> dependencies = getDependencies();

        return new LinkedHashSet<Artifact>( filter( allOf( matchers ), dependencies ) );
    }

    protected Artifact getDependency( Matcher<? super Artifact>... matchers )
    {
        return selectFirst( getDependencies(), allOf( matchers ) );
    }

    @SuppressWarnings( "unchecked" )
    protected Artifact getFrameworkConfig()
    {
        Artifact frmkCfg =
            getDependency( groupId( FRAMEWORK_GROUP_ID ), artifactId( "framework" ), classifier( "configs" ),
                           type( "zip" ) );

        // not on dependency list, trying to resolve it manually
        if ( frmkCfg == null )
        {
            frmkCfg = resolve( FRAMEWORK_GROUP_ID, "framework", getFrameworkVersion(), "configs", "zip" );
        }
        return frmkCfg;
    }

    @SuppressWarnings( "unchecked" )
    public String getFrameworkVersion()
    {
        Artifact dep = null;
        if ( dep == null )
        {
            dep = getDependency( groupId( "com.adobe.flex.framework" ), artifactId( "flex-framework" ), type( "pom" ) );
        }
        if ( dep == null )
        {
            dep = getDependency( groupId( "com.adobe.flex.framework" ), artifactId( "air-framework" ), type( "pom" ) );
        }
        if ( dep == null )
        {
            dep = getDependency( groupId( "com.adobe.flex.framework" ), artifactId( "framework" ), type( "swc" ) );
        }
        if ( dep == null )
        {
            dep = getDependency( groupId( "com.adobe.flex.framework" ), artifactId( "airframework" ), type( "swc" ) );
        }

        if ( dep == null )
        {
            return null;
        }
        return dep.getVersion();
    }

    @SuppressWarnings( "unchecked" )
    public <E> E getFromPluginContext( String key )
    {
        Object valueHolder = getPluginContext().get( key );
        if ( valueHolder instanceof ThreadLocal )
        {
            return ( (ThreadLocal<E>) valueHolder ).get();
        }
        return (E) valueHolder;
    }

    @SuppressWarnings( "unchecked" )
    protected Artifact getGlobalArtifact()
    {
        Artifact global = getDependency( GLOBAL_MATCHER );
        if ( global == null )
        {
            throw new IllegalArgumentException(
                                                "Global artifact is not available. Make sure to add 'playerglobal' or 'airglobal' to this project." );
        }

        File source = global.getFile();
        File dest =
            new File( source.getParentFile(), global.getClassifier() + "/" + global.getArtifactId() + "." + SWC );
        global.setFile( dest );

        try
        {
            if ( !dest.exists() )
            {
                dest.getParentFile().mkdirs();
                getLog().debug( "Striping global artifact, source: " + source + ", dest: " + dest );
                FileUtils.copyFile( source, dest );
            }
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Error renamming '" + global.getArtifactId() + "'.", e );
        }
        return global;
    }

    @SuppressWarnings( "unchecked" )
    public boolean getIsAirProject()
    {
        return getDependency( groupId( FRAMEWORK_GROUP_ID ), artifactId( AIR_GLOBAL ), type( SWC ) ) != null;
    }

    @Override
    @NotCacheable
    public Log getLog()
    {
        return this.log;
    }

    public Logger getMavenLogger()
    {
        return new OEMLogAdapter( new MavenLogger( getLog() ) );
    }

    public SinglePathResolver getMavenPathResolver()
    {
        return new MavenPathResolver( resources );
    }

    public File getOutputDirectory()
    {
        outputDirectory.mkdirs();
        return PathUtil.file( outputDirectory );
    }

    /**
     * @see org.apache.maven.plugin.ContextEnabled#getPluginContext()
     */
    @Override
    public Map<Object, Object> getPluginContext()
    {
        return pluginContext;
    }

    protected List<File> getResourcesTargetDirectories()
    {
        List<File> directories = new ArrayList<File>();
        for ( Resource resource : resources )
        {
            File directory;
            if ( resource.getTargetPath() != null )
            {
                directory = PathUtil.file( resource.getTargetPath(), getBasedir() );
            }
            else
            {
                directory = getOutputDirectory();
            }
            if ( !directory.isDirectory() )
            {
                continue;
            }

            directories.add( directory );
        }
        return directories;
    }

    public MavenSession getSession()
    {
        return session;
    }

    public File getTargetDirectory()
    {
        targetDirectory.mkdirs();
        return PathUtil.file( targetDirectory );
    }

    public File getUnpackedArtifact( String groupId, String artifactId, String version, String classifier, String type )
    {
        Artifact artifact = resolve( groupId, artifactId, version, classifier, type );

        String dirName = ( classifier == null ? "" : classifier ) + "_" + type;

        File dir = new File( artifact.getFile().getParentFile(), dirName );
        if ( dir.isDirectory() )
        {
            return dir;
        }

        dir.mkdirs();

        try
        {
            UnArchiver unarchive = archiverManager.getUnArchiver( artifact.getFile() );
            unarchive.setSourceFile( artifact.getFile() );
            unarchive.setDestDirectory( dir );
            unarchive.extract();
        }
        catch ( Exception e )
        {
            throw new MavenRuntimeException( "Failed to extract " + artifact, e );
        }

        return dir;
    }

    // TODO lazy load here would be awesome
    protected File getUnpackedFrameworkConfig()
    {
        Artifact frmkCfg = getFrameworkConfig();

        if ( frmkCfg == null )
        {
            return null;
        }

        return getUnpackedArtifact( frmkCfg.getGroupId(), frmkCfg.getArtifactId(), frmkCfg.getVersion(),
                                    frmkCfg.getClassifier(), frmkCfg.getType() );
    }

    @SuppressWarnings( "unchecked" )
    protected Matcher<? super Artifact> initGlobalMatcher()
    {
        return allOf( groupId( FRAMEWORK_GROUP_ID ), type( SWC ),//
                      anyOf( artifactId( PLAYER_GLOBAL ), artifactId( AIR_GLOBAL ) ) );
    }

    public boolean isSkip()
    {
        return skip;
    }

    @SuppressWarnings( "unchecked" )
    public <E> void putPluginContext( String key, E value )
    {
        Object valueHolder = getPluginContext().get( key );
        if ( !( valueHolder instanceof ThreadLocal ) )
        {
            valueHolder = new ThreadLocal<E>();
            getPluginContext().put( key, valueHolder );
        }
        ( (ThreadLocal<E>) valueHolder ).set( value );
    }

    public Artifact resolve( String groupId, String artifactId, String version, String classifier, String type )
        throws RuntimeMavenResolutionException
    {
        Artifact artifact =
            repositorySystem.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );
        if ( !artifact.isResolved() )
        {
            ArtifactResolutionRequest req = new ArtifactResolutionRequest();
            req.setArtifact( artifact );
            req.setLocalRepository( localRepository );
            req.setRemoteRepositories( remoteRepositories );
            ArtifactResolutionResult res = repositorySystem.resolve( req );
            if ( !res.isSuccess() )
            {
                if ( getLog().isDebugEnabled() )
                {
                    for ( Exception e : res.getExceptions() )
                    {
                        getLog().error( e );
                    }
                }
                throw new RuntimeMavenResolutionException( "Failed to resolve artifact " + artifact, res, artifact );
            }
        }
        return artifact;
    }

    protected DirectoryScanner scan( FileSet pattern )
    {
        return scan( pattern, PathUtil.file( pattern.getDirectory(), getBasedir() ) );
    }

    protected DirectoryScanner scan( PatternSet pattern, File directory )
    {
        if ( !directory.exists() )
        {
            return null;
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( directory );
        if ( !pattern.getIncludes().isEmpty() )
        {
            scanner.setIncludes( pattern.getIncludes().toArray( new String[0] ) );
        }
        if ( !pattern.getExcludes().isEmpty() )
        {
            scanner.setExcludes( pattern.getExcludes().toArray( new String[0] ) );
        }
        scanner.addDefaultExcludes();
        scanner.scan();
        return scanner;
    }

    protected DirectoryScanner scan( Resource resource )
    {
        File dir;
        if ( resource.getTargetPath() != null )
        {
            dir = PathUtil.file( resource.getTargetPath(), getBasedir() );
        }
        else
        {
            dir = PathUtil.file( resource.getDirectory(), getBasedir() );
        }

        return scan( resource, dir );
    }

    public void setArchiverManager( ArchiverManager archiverManager )
    {
        this.archiverManager = archiverManager;
    }

    @Override
    public void setLog( Log log )
    {
        this.log = log;
    }

    /**
     * @see org.apache.maven.plugin.ContextEnabled#setPluginContext(java.util.Map)
     */
    @SuppressWarnings( "all" )
    public void setPluginContext( Map pluginContext )
    {
        this.pluginContext = pluginContext;
    }

    protected String toClass( String filename )
    {
        String classname = filename;
        classname = classname.replaceAll( "\\.(.)*", "" );
        classname = classname.replace( '\\', '.' );
        classname = classname.replace( '/', '.' );
        return classname;
    }

    private List<String> toFilePattern( List<String> classesIncludes )
    {
        List<String> fileIncludes = new ArrayList<String>();
        for ( String classInclude : classesIncludes )
        {
            if ( classInclude.endsWith( MXML ) || classInclude.endsWith( AS ) )
            {
                fileIncludes.add( "**/" + classInclude );
            }
            else
            {
                fileIncludes.add( "**/" + classInclude.replace( '.', File.separatorChar ) + ".as" );
                fileIncludes.add( "**/" + classInclude.replace( '.', File.separatorChar ) + ".mxml" );
                fileIncludes.add( "**/" + classInclude.replace( '.', File.separatorChar ) + ".fxg" );
            }
        }
        return fileIncludes;
    }

    public void wait( Collection<Result> results )
        throws MojoFailureException, MojoExecutionException
    {
        for ( Result result : results )
        {
            checkResult( result );
        }
    }

    protected void wait( Result... results )
        throws MojoFailureException, MojoExecutionException
    {
        for ( Result result : results )
        {
            checkResult( result );
        }
    }

}
