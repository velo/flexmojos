package org.sonatype.flexmojos.plugin;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.artifactId;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.classifier;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.groupId;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.type;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.util.PathUtil.path;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.hamcrest.Matcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonatype.flexmojos.compatibilitykit.VersionUtils;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.plugin.common.flexbridge.MavenLogger;
import org.sonatype.flexmojos.plugin.common.flexbridge.MavenPathResolver;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenArtifact;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenRuntimeException;
import org.sonatype.flexmojos.plugin.compiler.lazyload.Cacheable;
import org.sonatype.flexmojos.plugin.compiler.lazyload.NotCacheable;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.util.CollectionUtils;
import org.sonatype.flexmojos.util.OSUtils;
import org.sonatype.flexmojos.util.PathUtil;

import flex2.compiler.Logger;
import flex2.compiler.common.SinglePathResolver;
import flex2.tools.oem.internal.OEMLogAdapter;

public abstract class AbstractMavenMojo
    implements Mojo, Cacheable, ContextEnabled
{

    private static final String AIR_GLOBAL = "airglobal";

    protected static final String COMPILER_GROUP_ID = "com.adobe.flex.compiler";

    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    protected static final String[] DEFAULT_RSL_URLS =
        new String[] { "/{contextRoot}/rsl/{artifactId}-{version}.{extension}" };

    public static final String DEFAULT_RUNTIME_LOCALE_OUTPUT_PATH =
        "/{contextRoot}/locales/{artifactId}-{version}-{locale}.{extension}";

    public static final String FRAMEWORK_GROUP_ID = "com.adobe.flex.framework";

    protected static final Matcher<? extends Artifact> GLOBAL_MATCHER = initGlobalMatcher();

    private static final String PLAYER_GLOBAL = "playerglobal";

    protected static final Answer<Object> RETURNS_NULL = new Answer<Object>()
    {
        public Object answer( InvocationOnMock invocation )
            throws Throwable
        {
            return null;
        }
    };

    @SuppressWarnings( "unchecked" )
    private static Matcher<? extends Artifact> initGlobalMatcher()
    {
        return allOf( groupId( FRAMEWORK_GROUP_ID ), type( SWC ),//
                      anyOf( artifactId( PLAYER_GLOBAL ), artifactId( AIR_GLOBAL ) ) );
    }

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
            reader =
                new InputStreamReader( getClass().getResourceAsStream( "/templates/test/air-descriptor-template.xml" ) );

            Map<String, String> variables = new LinkedHashMap<String, String>();
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

    protected String getAirTarget()
    {
        int[] version = VersionUtils.splitVersion( getCompilerVersion(), 3 );
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

    protected File getBasedir()
    {
        return basedir;
    }

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

    protected Set<Artifact> getDependencies( Matcher<? extends Artifact>... matchers )
    {
        Set<Artifact> dependencies = getDependencies();

        return new LinkedHashSet<Artifact>( filter( allOf( matchers ), dependencies ) );
    }

    protected Artifact getDependency( Matcher<? extends Artifact>... matchers )
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
            dep = getDependency( GLOBAL_MATCHER );
        }
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
            getDependency( groupId( "com.adobe.flex.framework" ), artifactId( "framework" ), type( "swc" ) );
        }
        if ( dep == null )
        {
            getDependency( groupId( "com.adobe.flex.framework" ), artifactId( "airframework" ), type( "swc" ) );
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
    public boolean getIsAirProject()
    {
        return getDependency( groupId( FRAMEWORK_GROUP_ID ), artifactId( AIR_GLOBAL ), type( SWC ) ) != null;
    }

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

    public File getTargetDirectory()
    {
        targetDirectory.mkdirs();
        return PathUtil.file( targetDirectory );
    }

    protected File getUnpackedArtifact( String groupId, String artifactId, String version, String classifier,
                                        String type )
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

    protected Artifact resolve( String groupId, String artifactId, String version, String classifier, String type )
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

    protected String[] resolveAdlVm( String command, MavenArtifact vmGav, String defaultArtifactId, String version,
                                     MavenArtifact runtimeGav )
    {

        String[] vm = resolveFlashVM( command, vmGav, defaultArtifactId, version );

        if ( vm == null )
        {
            return null;
        }

        if ( runtimeGav == null )
        {
            runtimeGav = new MavenArtifact();
            runtimeGav.setGroupId( "com.adobe.adl" );
            runtimeGav.setArtifactId( "runtime" );
            if ( OSUtils.isWindows() )
            {
                runtimeGav.setType( "zip" );
            }
            else
            {
                runtimeGav.setType( "tar.gz" );
                if ( OSUtils.isMacOS() )
                {
                    runtimeGav.setClassifier( "mac" );
                }
                else
                {
                    runtimeGav.setClassifier( "linux" );
                }
            }
        }

        if ( runtimeGav.getVersion() == null )
        {
            runtimeGav.setVersion( version );
        }

        // adl nedds air runtime, so lets grab it...
        File runtime =
            getUnpackedArtifact( runtimeGav.getGroupId(), runtimeGav.getArtifactId(), runtimeGav.getVersion(),
                                 runtimeGav.getClassifier(), runtimeGav.getType() );
        return CollectionUtils.merge( vm, new String[] { "-runtime", path( runtime ) } );
    }

    protected String[] resolveFlashVM( String command, MavenArtifact gav, String defaultArtifactId, String version )
    {
        if ( command != null )
        {
            getLog().debug( "Using user defined command for " + defaultArtifactId + ":" + command );
            return new String[] { command };
        }

        if ( gav == null )
        {
            gav = new MavenArtifact();
            gav.setGroupId( "com.adobe" );
            gav.setArtifactId( defaultArtifactId );
            if ( OSUtils.isWindows() )
            {
                gav.setType( "exe" );
            }
            else
            {
                gav.setType( "uexe" );
                if ( OSUtils.isMacOS() )
                {
                    gav.setClassifier( "mac" );
                }
                else
                {
                    gav.setClassifier( "linux" );
                }
            }

        }
        if ( gav.getVersion() == null )
        {
            gav.setVersion( version );
        }

        @SuppressWarnings( "unchecked" )
        Artifact vm =
            getDependency( groupId( gav.getGroupId() ), artifactId( gav.getArtifactId() ),
                           classifier( gav.getClassifier() ), type( gav.getType() ) );
        if ( vm == null )
        {
            try
            {
                vm =
                    resolve( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getClassifier(),
                             gav.getType() );
            }
            catch ( RuntimeMavenResolutionException e )
            {
                if ( getLog().isDebugEnabled() )
                {
                    getLog().error( e.getMessage(), e );
                }

                vm = null;
            }
        }

        if ( vm != null && vm.getFile() != null )
        {
            if ( !OSUtils.isWindows() )
            {
                vm.getFile().setExecutable( true );
            }
            getLog().debug( "Using " + defaultArtifactId + " from maven local repository: " + vm.getFile() );

            return new String[] { path( vm.getFile() ) };
        }
        else
        {
            getLog().debug( "Flexmojos was not able to resolve " + defaultArtifactId + " delegating the job to OS!" );

            return null;
        }
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
            scanner.setIncludes( (String[]) pattern.getIncludes().toArray( new String[0] ) );
        }
        if ( !pattern.getExcludes().isEmpty() )
        {
            scanner.setExcludes( (String[]) pattern.getExcludes().toArray( new String[0] ) );
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
            fileIncludes.add( classInclude.replace( '.', File.separatorChar ) + ".as" );
            fileIncludes.add( classInclude.replace( '.', File.separatorChar ) + ".mxml" );
        }
        return fileIncludes;
    }

    protected void wait( List<Result> results )
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
