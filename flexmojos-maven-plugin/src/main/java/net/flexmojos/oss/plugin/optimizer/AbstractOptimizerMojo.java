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
package net.flexmojos.oss.plugin.optimizer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;
import net.flexmojos.oss.compiler.FlexCompiler;
import net.flexmojos.oss.compiler.ICompilerConfiguration;
import net.flexmojos.oss.compiler.IOptimizerConfiguration;
import net.flexmojos.oss.plugin.AbstractMavenMojo;
import net.flexmojos.oss.plugin.utilities.ConfigurationResolver;
import net.flexmojos.oss.util.PathUtil;

import scala.None$;
import scala.Option;
import scala.Some;
import apparat.tools.reducer.MatryoshkaType;
import apparat.tools.reducer.Reducer.ReducerTool;
import apparat.tools.reducer.ReducerConfiguration;
import apparat.tools.stripper.Stripper.StripperTool;
import apparat.tools.stripper.StripperConfiguration;

public abstract class AbstractOptimizerMojo
    extends AbstractMavenMojo
{

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat( "#.##" );

    /**
     * @parameter expression="${project.build}"
     * @required
     * @readonly
     */
    protected Build build;

    /**
     * @component
     */
    protected FlexCompiler compiler;

    /**
     * The maven configuration directory
     * 
     * @parameter expression="${basedir}/src/main/config"
     * @required
     * @readonly
     */
    protected File configDirectory;

    /**
     * Keep the specified metadata in the SWF
     * <p>
     * Equivalent to -compiler.keep-as3-metadata
     * </p>
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
     * Load a file containing configuration options.
     * <p>
     * Equivalent to -load-config
     * </p>
     * Overwrite loadConfigs when defined!
     * 
     * @parameter expression="${flex.loadConfig}"
     */
    protected File loadConfig;

    /**
     * Load a file containing configuration options
     * <p>
     * Equivalent to -load-config
     * </p>
     * Usage:
     * 
     * <pre>
     * &lt;loadConfigs&gt;
     *   &lt;loadConfig&gt;???&lt;/loadConfig&gt;
     *   &lt;loadConfig&gt;???&lt;/loadConfig&gt;
     * &lt;/loadConfigs&gt;
     * </pre>
     * 
     * @parameter
     */
    protected File[] loadConfigs;

    /**
     * The project packaging type.
     * 
     * @parameter expression="${project.packaging}"
     * @required
     * @readonly
     */
    protected String packaging;

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
     * Use apparat to reduce the file size of SWFs by making use of the full feature set the Flash Player provides and
     * which is ignored by the ActionScript compiler.
     * <p>
     * Equivalent to apparat reducer
     * </p>
     * Reducer will only touch lossless images and not touch you encoded JPEG images
     * 
     * @parameter default-value="true" expression="${flex.reduce}"
     */
    protected boolean reduce;

    /**
     * Strength of deblocking filter
     * <p>
     * Equivalent to -d
     * </p>
     * 
     * @parameter expression="${flex.reduceDeblock}"
     */
    private float reduceDeblock;

    /**
     * Whether or not to use LZMA compression. Only available with SWF files.
     * 
     * @parameter expression="${flex.reduceLzma}" default-value="false"
     */
    private boolean reduceLzma;

    /**
     * Which Matryoshka implementation to use, valid values are: "none", "quiet" or "preloader".
     * 
     * @parameter expression="${flex.reduceMatryoshkaType}" default-value="none"
     */
    private String reduceMatryoshkaType;

    /**
     * A custom Matryoshka. Only used if the matryoshkaType is set to "custom".
     * 
     * @parameter expression="${flex.reduceMatryoshka}"
     */
    private File reduceMatryoshka;

    /**
     * Whether or not to merge ABC files into a single one.
     * <p>
     * Equivalent to -d
     * </p>
     * 
     * @parameter expression="${flex.reduceMergeABC}" default-value="true"
     */
    private boolean reduceMergeABC;

    /**
     * Compression quality from 0.0 to 1.0
     * <p>
     * Equivalent to -q
     * </p>
     * 
     * @parameter expression="${flex.reduceQuality}"
     */
    private float reduceQuality;

    /**
     * Whether or not to sort the constant pool. Only if <code>reduceMergeABC</code> is specified.
     * 
     * @parameter expression="${flex.reduceSortCPool}" default-value="true"
     */
    private boolean reduceSortCPool;

    /**
     * Whether or not to merge control flow where possible.
     * 
     * @parameter expression="${flex.reduceMergeCF}" default-value="true"
     */
    private boolean reduceMergeCF;

    /**
     * Use apparat to strip
     * <p>
     * Equivalent to apparat stripper
     * </p>
     * 
     * @parameter default-value="true" expression="${flex.strip}"
     */
    protected boolean strip;

    /**
     * Name of the Flex Tool Group that should be used for the build.
     *
     * @parameter expression="${flex.compilerName}"
     */
    protected String compilerName;

    public abstract String getInput();

    public String[] getKeepAs3Metadata()
    {
        return keepAs3Metadatas;
    }

    public String[] getLoadConfig()
    {
        return PathUtil.paths( ConfigurationResolver.resolveConfiguration( loadConfigs, loadConfig, configDirectory ) );
    }

    public IOptimizerConfiguration getOptimizerConfiguration( File input, File output )
    {
        // mocking real code doesn't seem to be a good idea, but produces a much cleaner code
        IOptimizerConfiguration cfg = mock( IOptimizerConfiguration.class, RETURNS_NULL );
        ICompilerConfiguration compilerCfg = mock( ICompilerConfiguration.class, RETURNS_NULL );
        when( cfg.getLoadConfig() ).thenReturn( getLoadConfig() );
        when( cfg.getInput() ).thenReturn( PathUtil.path( input ) );
        when( cfg.getOutput() ).thenReturn( PathUtil.path( output ) );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        when( compilerCfg.getKeepAs3Metadata() ).thenReturn( getKeepAs3Metadata() );
        return cfg;
    }

    public String getOutput()
    {
        return PathUtil.path( new File( build.getDirectory(), build.getFinalName() + ".swf" ) );
    }

    protected File optimize()
        throws MojoFailureException, MojoExecutionException
    {
        File input = PathUtil.file( getInput() );
        double originalSize = input.length();
        {
            getLog().debug( "Backuping original file " + input );
            final File output = new File( project.getBuild().getOutputDirectory(), "original.swf" );
            try
            {
                FileUtils.copyFile( input, output );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
            input = output;
        }

        input = optimize( input );

        if ( reduce )
        {
            getLog().debug( "Reducing" );
            final File output = new File( project.getBuild().getOutputDirectory(), "reduced.swf" );
            reduce( input, output );
            input = output;
        }

        if ( strip )
        {
            getLog().debug( "Stripping" );
            final File output = new File( project.getBuild().getOutputDirectory(), "stripped.swf" );
            reduce( input, output );
            input = output;
        }

        {
            getLog().debug( "Placing optimized file on target folder" );
            final File output = PathUtil.file( getOutput() );
            try
            {
                FileUtils.copyFile( input, output );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }

            double finalSize = output.length();
            double rate = ( finalSize / originalSize ) * 100;

            getLog().info( "Optimization result: " + DECIMAL_FORMAT.format( rate ) + "%" );

            return output;
        }
    }

    protected abstract File optimize( File input )
        throws MojoFailureException, MojoExecutionException;

    protected void optimize( File input, File output )
        throws MojoFailureException, MojoExecutionException
    {
        int result;
        try
        {
            result = compiler.optimize( getOptimizerConfiguration( input, output ), true, compilerName ).getExitCode();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        if ( result != 0 )
        {
            throw new MojoFailureException( "Got " + result + " errors building project, check logs" );
        }
    }

    protected void reduce( final File input, final File output )
    {
        ReducerTool s = new ReducerTool();
        ReducerConfiguration cfg = new ReducerConfiguration()
        {

            public float deblock()
            {
                return reduceDeblock;
            }

            public File input()
            {
                return input;
            }

            public boolean lzma()
            {
                return reduceLzma;
            }

            public int matryoshkaType()
            {
                if ( reduceMatryoshkaType.equalsIgnoreCase( "quiet" ) )
                {
                    return MatryoshkaType.QUIET();
                }
                else if ( reduceMatryoshkaType.equalsIgnoreCase( "preloader" ) )
                {
                    return MatryoshkaType.PRELOADER();
                }
                else if ( reduceMatryoshkaType.equalsIgnoreCase( "custom" ) )
                {
                    return MatryoshkaType.CUSTOM();
                }
                else
                {
                    return MatryoshkaType.NONE();
                }
            }

            @SuppressWarnings( "unchecked" )
            public Option<File> matryoshka()
            {
                return ( null == reduceMatryoshka ) ? None$.MODULE$ : new Some( reduceMatryoshka );
            }

            public boolean mergeABC()
            {
                return reduceMergeABC;
            }

            public File output()
            {
                return output;
            }

            public float quality()
            {
                return reduceQuality;
            }

            public boolean sortCPool()
            {
                return reduceSortCPool;
            }

            public boolean mergeCF()
            {
                return reduceMergeCF;
            }
        };
        s.configure( cfg );
        s.run();
    }

    protected void strip( final File input, final File output )
    {
        StripperTool s = new StripperTool();
        StripperConfiguration cfg = new StripperConfiguration()
        {
            public File input()
            {
                return input;
            }

            public File output()
            {
                return output;
            }
        };
        s.configure( cfg );
        s.run();
    }

}