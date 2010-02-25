package org.sonatype.flexmojos.compiler;

import static org.sonatype.flexmojos.common.FlexExtension.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import static org.mockito.Mockito.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.common.AbstractMavenFlexCompilerConfiguration;
import org.sonatype.flexmojos.common.converter.Module;
import org.sonatype.flexmojos.test.util.PathUtil;
import org.sonatype.flexmojos.truster.FlashPlayerTruster;
import org.sonatype.flexmojos.truster.TrustException;
import org.sonatype.flexmojos.utilities.SourceFileResolver;
import static org.sonatype.flexmojos.common.FlexExtension.*;

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
 * @since 4.0
 * @goal compile-swf
 * @requiresDependencyResolution compile
 * @phase compile
 * @configurator flexmojos
 */
public class MxmlcMojo
    extends AbstractMavenFlexCompilerConfiguration<MxmlcConfigurationHolder>
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
     * A list of resource bundles to include in the output SWC
     * <p>
     * Equivalent to -include-resource-bundles
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;includeResourceBundles&gt;
     *   &lt;rb&gt;SharedResources&lt;/rb&gt;
     *   &lt;rb&gt;Collections&lt;/rb&gt;
     * &lt;/includeResourceBundles&gt;
     * </pre>
     * 
     * @parameter
     */
    private List<String> includeResourceBundles;

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
     * @parameter
     */
    private Module[] modules;

    /**
     * @component
     * @required
     * @readonly
     */
    private FlashPlayerTruster truster;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        executeCompiler( new MxmlcConfigurationHolder( this, getSourceFile() ) );

        if ( runtimeLocales != null )
        {
            for ( String locale : runtimeLocales )
            {
                ICommandLineConfiguration cfg = spy( this );
                ICompilerConfiguration compilerCfg = spy( this.getCompilerConfiguration() );
                when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
                when( compilerCfg.getLocale() ).thenReturn( new String[] { locale } );
                executeCompiler( new MxmlcConfigurationHolder( cfg, null ) );
            }
        }

        executeModules();
    }

    private void executeModules()
        throws MojoExecutionException, MojoFailureException
    {
        if ( modules != null )
        {
            for ( Module module : modules )
            {
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
                moduleOutputDir.mkdirs();

                File moduleOutput = new File( moduleOutputDir, moduleFinalName );

                // TODO include the original extern
                String[] loadExtern = new String[] { getLinkReport() };

                MxmlcMojo cfg = (MxmlcMojo) this.clone();
                cfg.getCache().put( "getClassifier", classifier );
                cfg.getCache().put( "getTargetDirectory", moduleOutputDir );
                cfg.getCache().put( "getFinalName", moduleFinalName );
                cfg.getCache().remove( "getOutput" );
                if ( module.isOptimize() )
                {
                    cfg.getCache().put( "getLoadExterns", loadExtern );
                }
                executeCompiler( new MxmlcConfigurationHolder( cfg, moduleSource ) );
            }
        }
    }

    public final int doCompile( MxmlcConfigurationHolder cfg )
        throws Exception
    {
        try
        {
            return compiler.compileSwf( cfg );
        }
        finally
        {
            truster.updateSecuritySandbox( PathUtil.getCanonicalFile( cfg.getConfiguration().getOutput() ) );
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

    public String getProjector()
    {
        return projector;
    }

    protected File getSourceFile()
    {
        return SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), sourceFile, project.getGroupId(),
                                                     project.getArtifactId() );
    }

    @Override
    public final String getProjectType()
    {
        return SWF;
    }

}
