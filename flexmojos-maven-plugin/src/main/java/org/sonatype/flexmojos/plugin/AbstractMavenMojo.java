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
package org.sonatype.flexmojos.plugin;

import static ch.lambdaj.Lambda.filter;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.artifactId;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.classifier;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.groupId;
import static org.sonatype.flexmojos.matcher.artifact.ArtifactMatcher.type;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;

import java.io.File;
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
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
import org.hamcrest.Matcher;
import org.mockito.ReturnValues;
import org.mockito.invocation.InvocationOnMock;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenRuntimeException;
import org.sonatype.flexmojos.plugin.compiler.lazyload.Cacheable;
import org.sonatype.flexmojos.util.PathUtil;

public abstract class AbstractMavenMojo
    implements Mojo, Cacheable, ContextEnabled
{

    private static final String AIR_GLOBAL = "airglobal";

    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    protected static final String[] DEFAULT_RSL_URLS =
        new String[] { "/{contextRoot}/rsl/{artifactId}-{version}.{extension}" };

    public static final String FRAMEWORK_GROUP_ID = "com.adobe.flex.framework";

    protected static final Matcher<? extends Artifact> GLOBAL_MATCHER = initGlobalMatcher();

    private static final String PLAYER_GLOBAL = "playerglobal";

    protected static final ReturnValues RETURNS_NULL = new ReturnValues()
    {
        public Object valueFor( InvocationOnMock invocation )
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
            fs.setDirectory( PathUtil.getCanonicalPath( file ) );
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

    protected List<String> filterClasses( List<FileSet> classesPattern, File[] directories )
    {
        directories = PathUtil.getExistingFiles( directories );

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
                File dir = PathUtil.getCanonicalFile( pattern.getDirectory(), getBasedir() );
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
        directories = PathUtil.getExistingFilesList( directories );

        Set<File> includedFiles = new LinkedHashSet<File>();
        for ( FileSet pattern : patterns )
        {
            if ( pattern.getDirectory() == null )
            {
                for ( File dir : directories )
                {
                    DirectoryScanner scan = scan( pattern, dir );
                    includedFiles.addAll( PathUtil.getCanonicalFiles( scan.getIncludedFiles(), dir ) );
                }
            }
            else
            {
                File dir = PathUtil.getCanonicalFile( pattern.getDirectory(), getBasedir() );
                if ( !directories.contains( dir ) )
                {
                    throw new IllegalArgumentException( "Pattern does point to an invalid directory: "
                        + dir.getAbsolutePath() );
                }

                includedFiles.addAll( PathUtil.getCanonicalFiles( scan( pattern, dir ).getIncludedFiles(), dir ) );
            }
        }

        return includedFiles;
    }

    protected File getBasedir()
    {
        return basedir;
    }

    public Map<String, Object> getCache()
    {
        return cache;
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

        Set<Artifact> dependencies = getDependencies();
        List<Artifact> filtered = filter( allOf( matchers ), dependencies );
        if ( filtered.isEmpty() )
        {
            return null;
        }

        return filtered.get( 0 );
    }

    // TODO lazy load here would be awesome
    @SuppressWarnings( "unchecked" )
    protected Artifact getFrameworkConfig()
    {
        Artifact frmkCfg =
            getDependency( groupId( FRAMEWORK_GROUP_ID ), artifactId( "framework" ), classifier( "configs" ),
                           type( "zip" ) );

        // not on dependency list, trying to resolve it manually
        if ( frmkCfg == null )
        {
            // it should resolve playerglobal or airglobal, framework can be absent
            Artifact frmk = getDependency( groupId( FRAMEWORK_GROUP_ID ), artifactId( "framework" ) );

            if ( frmk == null )
            {
                return null;
            }

            frmkCfg = resolve( FRAMEWORK_GROUP_ID, "framework", frmk.getVersion(), "configs", "zip" );
        }
        return frmkCfg;
    }

    @SuppressWarnings( "unchecked" )
    public boolean getIsAirProject()
    {
        return getDependency( groupId( FRAMEWORK_GROUP_ID ), artifactId( AIR_GLOBAL ), type( SWC ) ) != null;
    }

    public Log getLog()
    {
        return this.log;
    }

    public File getOutputDirectory()
    {
        outputDirectory.mkdirs();
        return PathUtil.getCanonicalFile( outputDirectory );
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
                directory = PathUtil.getCanonicalFile( resource.getTargetPath(), getBasedir() );
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
        return PathUtil.getCanonicalFile( targetDirectory );
    }

    // TODO lazy load here would be awesome
    protected File getUnpackedFrameworkConfig()
    {
        Artifact frmkCfg = getFrameworkConfig();

        if ( frmkCfg == null )
        {
            return null;
        }

        File cfgZip = frmkCfg.getFile();
        File dest = new File( getOutputDirectory(), "configs" );
        dest.mkdirs();

        try
        {
            UnArchiver unzip = archiverManager.getUnArchiver( cfgZip );
            unzip.setSourceFile( cfgZip );
            unzip.setDestDirectory( dest );
            unzip.extract();
        }
        catch ( Exception e )
        {
            throw new MavenRuntimeException( "Failed to unpack framework configuration", e );
        }

        return dest;
    }

    public boolean isSkip()
    {
        return skip;
    }

    protected Artifact resolve( String groupId, String artifactId, String version, String classifier, String type )
    {
        Artifact artifact =
            repositorySystem.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );
        if ( !artifact.isResolved() )
        {
            ArtifactResolutionRequest req = new ArtifactResolutionRequest();
            req.setArtifact( artifact );
            req.setLocalRepository( localRepository );
            req.setRemoteRepositories( remoteRepositories );
            // FIXME need to check isSuccess
            repositorySystem.resolve( req ).isSuccess();
        }
        return artifact;
    }

    protected DirectoryScanner scan( FileSet pattern )
    {
        return scan( pattern, PathUtil.getCanonicalFile( pattern.getDirectory(), getBasedir() ) );
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
            dir = PathUtil.getCanonicalFile( resource.getTargetPath(), getBasedir() );
        }
        else
        {
            dir = PathUtil.getCanonicalFile( resource.getDirectory(), getBasedir() );
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
    @SuppressWarnings( "unchecked" )
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