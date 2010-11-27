package org.sonatype.flexmojos.plugin.compiler;

import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWF;
import static org.sonatype.flexmojos.util.PathUtil.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.MxmlcConfigurationHolder;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.plugin.compiler.attributes.converter.Module;
import org.sonatype.flexmojos.plugin.utilities.SourceFileResolver;
import org.sonatype.flexmojos.truster.FlashPlayerTruster;
import org.sonatype.flexmojos.util.PathUtil;

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
 * @configurator flexmojos
 * @threadSafe
 */
public class MxmlcMojo
    extends AbstractFlexCompilerMojo<MxmlcConfigurationHolder, MxmlcMojo>
    implements ICommandLineConfiguration, Mojo
{

    /**
     * DOCME Again, undocumented by adobe
     * <p>
     * Equivalent to -file-specs
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;fileSpecs&gt;
     *   &lt;fileSpec&gt;???&lt;/fileSpec&gt;
     *   &lt;fileSpec&gt;???&lt;/fileSpec&gt;
     * &lt;/fileSpecs&gt;
     * </pre>
     * 
     * @parameter
     */
    private List<String> fileSpecs;

    /**
     * The list of modules to be compiled.
     * 
     * <pre>
     * &lt;modules&gt;
     *   &lt;module&gt;Module1.mxml&lt;/module&gt;
     *   &lt;module&gt;Module2.mxml&lt;/module&gt;
     *   &lt;module&gt;
     *     &lt;sourceFile&gt;Module3.mxml&lt;/sourceFile&gt;
     *     &lt;optimize&gt;false&lt;/optimize&gt;
     *     &lt;finalName&gt;MyModule&lt;/finalName&gt;
     *     &lt;destinationPath&gt;dir1/dir2&lt;/destinationPath&gt;
     *   &lt;/module&gt;
     * &lt;/modules&gt;
     * </pre>
     * 
     * @parameter
     */
    private Module[] modules;

    /**
     * When true, tells flexmojos to optimized modules using link reports/load externs
     * 
     * @parameter expression="${flex.modulesLoadExterns}" default-value="true"
     */
    private boolean modulesLoadExterns;

    /**
     * DOCME Another, undocumented by adobe
     * <p>
     * Equivalent to -projector
     * </p>
     * 
     * @parameter expression="${flex.projector}"
     */
    private String projector;

    /**
     * The file to be compiled. The path must be relative with source folder
     * 
     * @parameter expression="${flex.sourceFile}"
     */
    private String sourceFile;

    /**
     * @component
     * @required
     * @readonly
     */
    private FlashPlayerTruster truster;

    /**
     * When true, flexmojos will register register every compiled SWF files as trusted. These SWF files are assigned to
     * the local-trusted sandbox. They can interact with any other SWF files, and they can load data from anywhere,
     * remote or local. On false nothing is done, so if the file is already trusted it will still as it is.
     * 
     * @parameter default-value="true" expression="${updateSecuritySandbox}"
     */
    private boolean updateSecuritySandbox;

    public final Result doCompile( MxmlcConfigurationHolder cfg, boolean synchronize )
        throws Exception
    {
        if ( isUpdateSecuritySandbox() )
        {
            truster.updateSecuritySandbox( PathUtil.file( cfg.getConfiguration().getOutput() ) );
        }
        return compiler.compileSwf( cfg, synchronize );
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !PathUtil.existAny( getSourcePath() ) )
        {
            getLog().warn( "Skipping compiler, source path doesn't exist. " + Arrays.toString( getSourcePath() ) );
            return;
        }

        executeCompiler( new MxmlcConfigurationHolder( this, getSourceFile() ), true );
        if ( !file( getOutput() ).exists() )
        {
            throw new IllegalStateException( "Output file doesn't exist and now error was throw by the compiler!" );
        }

        if ( getLocalesRuntime() != null )
        {
            List<Result> results = new ArrayList<Result>();
            for ( String locale : getLocalesRuntime() )
            {
                MxmlcMojo cfg = this.clone();
                configureResourceBundle( locale, cfg );
                results.add( executeCompiler( new MxmlcConfigurationHolder( cfg, null ), fullSynchronization ) );
            }

            wait( results );
        }

        if ( getModules() != null )
        {
            List<Result> results = new ArrayList<Result>();

            for ( Module module : getModules() )
            {
                if ( module.isOptimize() == null )
                {
                    module.setOptimize( modulesLoadExterns );
                }

                File moduleSource =
                    SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), module.getSourceFile() );

                String classifier = FilenameUtils.getBaseName( moduleSource.getName() ).toLowerCase();

                String moduleFinalName;
                if ( module.getFinalName() != null )
                {
                    moduleFinalName = module.getFinalName();
                }
                else
                {
                    moduleFinalName = project.getBuild().getFinalName() + "-" + classifier;
                }

                File moduleOutputDir;
                if ( module.getDestinationPath() != null )
                {
                    moduleOutputDir = new File( project.getBuild().getDirectory(), module.getDestinationPath() );
                }
                else
                {
                    moduleOutputDir = new File( project.getBuild().getDirectory() );
                }

                // TODO include the original extern
                List<String> loadExterns = new ArrayList<String>();
                loadExterns.add( getLinkReport() );
                if ( getLoadExterns() != null )
                {
                    loadExterns.addAll( Arrays.asList( getLoadExterns() ) );
                }

                MxmlcMojo cfg = this.clone();
                cfg.classifier = classifier;
                cfg.targetDirectory = moduleOutputDir;
                cfg.finalName = moduleFinalName;
                if ( module.isOptimize() )
                {
                    cfg.getCache().put( "getLoadExterns", loadExterns.toArray( new String[1] ) );
                }
                results.add( executeCompiler( new MxmlcConfigurationHolder( cfg, moduleSource ), fullSynchronization ) );
            }

            wait( results );
        }
    }

    public List<String> getFileSpecs()
    {
        return fileSpecs;
    }

    public List<String> getIncludeResourceBundles()
    {
        return includeResourceBundles;
    }

    @Override
    public String[] getLocale()
    {
        String[] locales = super.getLocale();
        if ( locales != null )
        {
            return locales;
        }

        if ( "css".equalsIgnoreCase( FilenameUtils.getExtension( sourceFile ) ) )
        {
            return new String[] {};
        }

        return new String[] { toolsLocale };

    }

    public Module[] getModules()
    {
        return modules;
    }

    public String getProjector()
    {
        return projector;
    }

    @Override
    public final String getProjectType()
    {
        return SWF;
    }

    protected File getSourceFile()
    {
        return SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), sourceFile, project.getGroupId(),
                                                     project.getArtifactId() );
    }

    public boolean isUpdateSecuritySandbox()
    {
        return updateSecuritySandbox;
    }
}
