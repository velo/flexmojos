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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.PatternSet;
import org.apache.maven.model.Resource;
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
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.plugin.compiler.attributes.MavenRuntimeException;
import org.sonatype.flexmojos.util.PathUtil;

public abstract class AbstractMavenMojo
    implements Mojo
{

    private static final String AIR_GLOBAL = "airglobal";

    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat();

    protected static final String[] DEFAULT_RSL_URLS =
        new String[] { "/{contextRoot}/rsl/{artifactId}-{version}.{extension}" };

    public static final String FRAMEWORK_GROUP_ID = "com.adobe.flex.framework";

    protected static final Matcher<? extends Artifact> GLOBAL_MATCHER = initGlobalMatcher();

    private static final String PLAYER_GLOBAL = "playerglobal";

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
     * Skips lexmojos goal execution
     * 
     * @parameter expression="${flexmojos.skip}"
     */
    protected boolean skip;

    public AbstractMavenMojo()
    {
        super();
    }

    protected FileSet[] as3ClassesFileSet( File... files )
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

        return sets.toArray( new FileSet[0] );
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

    protected List<String> filterClasses( PatternSet[] classesPattern, File[] directories )
    {
        List<String> classes = new ArrayList<String>();

        for ( File directory : directories )
        {
            if ( !directory.exists() )
            {
                continue;
            }

            for ( PatternSet pattern : classesPattern )
            {
                if ( pattern instanceof FileSet )
                {
                    File dir = PathUtil.getCanonicalFile( ( (FileSet) pattern ).getDirectory() );
                    if ( !ArrayUtils.contains( directories, dir ) )
                    {
                        throw new IllegalArgumentException( "Pattern does point to an invalid source directory: "
                            + dir.getAbsolutePath() );
                    }
                }

                DirectoryScanner scanner = scan( directory, pattern );

                String[] included = scanner.getIncludedFiles();
                for ( String file : included )
                {
                    String classname = file;
                    classname = classname.replaceAll( "\\.(.)*", "" );
                    classname = classname.replace( '\\', '.' );
                    classname = classname.replace( '/', '.' );
                    classes.add( classname );
                }
            }
        }

        return classes;
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
        return outputDirectory;
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

    protected DirectoryScanner scan( File directory, PatternSet pattern )
    {
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

    public void setLog( Log log )
    {
        this.log = log;
    }

    protected void wait( List<Result> results )
        throws MojoFailureException, MojoExecutionException
    {
        for ( Result result : results )
        {
            checkResult( result );
        }
    }
}