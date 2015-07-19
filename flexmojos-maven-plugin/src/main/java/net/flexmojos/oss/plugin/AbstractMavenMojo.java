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

import flex2.compiler.Logger;
import flex2.compiler.common.SinglePathResolver;
import flex2.tools.oem.internal.OEMLogAdapter;
import net.flexmojos.oss.compiler.command.Result;
import net.flexmojos.oss.compiler.util.ThreadLocalToolkitHelper;
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

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.selectFirst;
import static net.flexmojos.oss.matcher.artifact.ArtifactMatcher.*;
import static net.flexmojos.oss.plugin.common.FlexExtension.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;

public abstract class AbstractMavenMojo
    implements Mojo, Cacheable, ContextEnabled
{

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    public static final String[] DEFAULT_RSL_URLS =
        new String[] { "/{contextRoot}/rsl/{artifactId}-{version}.{extension}" };

    public static final String DEFAULT_RUNTIME_LOCALE_OUTPUT_PATH =
        "/{contextRoot}/locales/{artifactId}-{version}-{locale}.{extension}";

    public static final String AIR_GROUP_ID = "com.adobe.air.framework";
    public static final String FLASH_GROUP_ID = "com.adobe.flash.framework";

    public static final String AIR_GLOBAL = "airglobal";
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
     * Adobe Flash version
     *
     * @parameter expression="${flex.flashVersion}"
     */
    protected String flashVersion;

    /**
     * Adobe AIR version
     *
     * @parameter expression="${flex.airVersion}"
     */
    protected String airVersion;

    protected final Matcher<? extends Artifact> GLOBAL_MATCHER = initGlobalMatcher();

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
     * @parameter expression="${project.build.directory}"
     * @readonly
     * @required
     */
    private File buildDirectory;

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
        directories = PathUtil.existingFiles(directories);

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
        directories = PathUtil.existingFilesList(directories);

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
            // Get the Air-Version from the air runtime artifact (airglobal.swc)
            Artifact airRuntimeArtifact = getDependency(
                    allOf(groupId(AIR_GROUP_ID), artifactId(AIR_GLOBAL), type(SWC)));
            if(airRuntimeArtifact != null) {
                return airRuntimeArtifact.getVersion();
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

    protected Artifact getCompilerArtifact()
    {
        Artifact apacheCompiler = MavenUtils.searchFor(pluginArtifacts, "org.apache.flex", "compiler", null, "pom", null);
        if(apacheCompiler != null) {
            return apacheCompiler;
        }
        Artifact apacheFalconCompiler = MavenUtils.searchFor(pluginArtifacts, "org.apache.flex.compiler", "falcon-compiler", null, "jar", null);
        if(apacheFalconCompiler != null) {
            return apacheFalconCompiler;
        }
        return null;
    }

    protected Artifact getFrameworkArtifact()
    {
        Artifact apacheFramework = MavenUtils.searchFor(getDependencies(), "org.apache.flex", "framework", null, "pom", null);
        if(apacheFramework != null) {
            return apacheFramework;
        }
        return null;
    }


    public String getFdkGroupId()
    {
        Artifact compilerArtifact = getCompilerArtifact();
        if(compilerArtifact != null) {
            return compilerArtifact.getGroupId();
        }
        return null;
    }

    public String getCompilerGroupId()
    {
        final Artifact compilerArtifact = getCompilerArtifact();
        if(compilerArtifact != null) {
            return compilerArtifact.getGroupId();
        }
        return null;
    }

    public String getFrameworkGroupId()
    {
        final Artifact frameworkArtifact = getFrameworkArtifact();
        if(frameworkArtifact != null) {
            return frameworkArtifact.getGroupId() + ".framework";
        }
        return null;
    }

    public String getCompilerVersion()
    {
        Artifact compilerArtifact = getCompilerArtifact();
        if(compilerArtifact != null) {
            return compilerArtifact.getVersion();
        }
        return null;
    }

    public String getFrameworkArtifactVersion(String groupId, String artifactId)
    {
        final Artifact frameworkArtifact = getFrameworkArtifact();
        if(frameworkArtifact != null) {
            return frameworkArtifact.getVersion();
        }
        return null;
    }

    public Set<Artifact> getDependencies()
    {
        return Collections.unmodifiableSet(project.getArtifacts());
    }

    protected Set<Artifact> getDependencies( Matcher<? extends Artifact>... matchers )
    {
        Set<Artifact> dependencies = getDependencies();

        return new LinkedHashSet<Artifact>( filter( allOf( matchers ), dependencies ) );
    }

    protected Artifact getDependency( Matcher<? extends Artifact>... matchers )
    {
        return selectFirst(getDependencies(), allOf(matchers));
    }

    @SuppressWarnings( "unchecked" )
    protected Artifact getFrameworkConfig()
    {
        if(getFrameworkGroupId() == null) {
            return null;
        }
        Matcher<? extends Artifact>[] frmkCfgMatchers = new  Matcher[] {
                groupId( getFrameworkGroupId() ), artifactId( "framework" ), classifier( "configs" ),
                type( "zip" )
        };
        Artifact frmkCfg =
            getDependency(frmkCfgMatchers);

        // not on dependency list, trying to resolve it manually
        if ( frmkCfg == null )
        {
            frmkCfg = resolve( getFrameworkGroupId(), "framework",
                    getFrameworkArtifactVersion(getFrameworkGroupId(), "framework"), "configs", "zip" );
        }
        return frmkCfg;
    }

    @SuppressWarnings( "unchecked" )
    public String getFrameworkVersion()
    {
        Artifact dep = getDependency(
                groupId("org.apache.flex.framework"), artifactId("framework"), type("swc"));

        if ( dep == null )
        {
            return null;
        }
        return dep.getVersion();
    }

    @SuppressWarnings( "unchecked" )
    public <E> E getFromPluginContext( String key )
    {
        Object valueHolder = getPluginContext().get(key);
        if ( valueHolder instanceof ThreadLocal )
        {
            return ( (ThreadLocal<E>) valueHolder ).get();
        }
        return (E) valueHolder;
    }

    @SuppressWarnings( "unchecked" )
    protected Artifact getGlobalArtifact()
    {
        Artifact global = getDependency(GLOBAL_MATCHER);
        if ( global == null )
        {
            throw new IllegalArgumentException(
                    "Global artifact is not available. Make sure to add " +
                            "'playerglobal' or 'airglobal' to this project.");
        }

        File source = global.getFile();
        File dest = new File( source.getParentFile(), global.getArtifactId() + "." + SWC );
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
            throw new IllegalStateException( "Error renaming '" + global.getArtifactId() + "'.", e );
        }
        return global;
    }

    public Artifact getAirGlobal() {
        return getDependency( groupId( AIR_GROUP_ID ), artifactId( AIR_GLOBAL ), type( SWC ) );
    }

    @SuppressWarnings( "unchecked" )
    public boolean getIsAirProject()
    {
        return (getAirGlobal() != null);
    }

    public String getAirVersion() {
        if(getIsAirProject()) {
            return getAirGlobal().getVersion();
        }
        return null;
    }

    public Artifact getFlashGlobal() {
        return getDependency( groupId( FLASH_GROUP_ID ), artifactId( PLAYER_GLOBAL ), type( SWC ) );
    }

    @SuppressWarnings( "unchecked" )
    public boolean getIsFlashProject()
    {
        return (getFlashGlobal() != null);
    }

    public String getFlashVersion() {
        if(getIsFlashProject()) {
            return getFlashGlobal().getVersion();
        }
        return null;
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

    public File getBuildDirectory()
    {
        buildDirectory.mkdirs();
        return PathUtil.file( buildDirectory );
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
        return PathUtil.file(targetDirectory);
    }

    public File getUnpackedArtifact( String groupId, String artifactId, String version, String classifier, String type ) {
        return getUnpackedArtifact(null, groupId, artifactId, version, classifier, type);
    }

    public File getUnpackedArtifact( File destDir,  String groupId, String artifactId, String version, String classifier, String type )
    {
        Artifact artifact = resolve( groupId, artifactId, version, classifier, type );

        File dir;
        if(destDir == null) {
            String dirName = ( classifier == null ? "" : classifier ) + "_" + type;
            dir = new File( artifact.getFile().getParentFile(), dirName );
        } else {
            dir = destDir;
        }
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

        return getUnpackedArtifact(frmkCfg.getGroupId(), frmkCfg.getArtifactId(), frmkCfg.getVersion(),
                frmkCfg.getClassifier(), frmkCfg.getType());
    }

    @SuppressWarnings( "unchecked" )
    protected Matcher<? extends Artifact> initGlobalMatcher()
    {
        return anyOf(allOf(groupId(AIR_GROUP_ID), artifactId(AIR_GLOBAL), type(SWC)),
                allOf(groupId(FLASH_GROUP_ID), artifactId(PLAYER_GLOBAL), type(SWC)));
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
            repositorySystem.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
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
            List<String> includes = pattern.getIncludes();
            scanner.setIncludes(includes.toArray(new String[includes.size()]));
        }
        if ( !pattern.getExcludes().isEmpty() )
        {
            List<String> excludes = pattern.getExcludes();
            scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ThreadLocalToolkitHelper.setMavenLogger(getMavenLogger());
        ThreadLocalToolkitHelper.setMavenResolver(getMavenPathResolver() );
        fmExecute();
    }

    public abstract void fmExecute() throws MojoExecutionException, MojoFailureException;

}
