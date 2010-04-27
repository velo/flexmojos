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
package org.sonatype.flexmojos.plugin.optimizer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;
import org.mockito.ReturnValues;
import org.mockito.invocation.InvocationOnMock;
import org.sonatype.flexmojos.compiler.FlexCompiler;
import org.sonatype.flexmojos.compiler.ICompilerConfiguration;
import org.sonatype.flexmojos.compiler.IOptimizerConfiguration;
import org.sonatype.flexmojos.plugin.utilities.ConfigurationResolver;
import org.sonatype.flexmojos.util.PathUtil;

import apparat.tools.ApparatConfiguration;
import apparat.tools.reducer.Reducer.ReducerTool;
import apparat.tools.stripper.Stripper.StripperTool;

public abstract class AbstractOptimizerMojo
    extends AbstractMojo
{

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat( "#.##" );

    private static final ReturnValues RETURNS_NULL = new ReturnValues()
    {
        public Object valueFor( InvocationOnMock invocation )
        {
            return null;
        }
    };

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
     * Use apparat to strip
     * <p>
     * Equivalent to apparat stripper
     * </p>
     * 
     * @parameter default-value="true" expression="${flex.strip}"
     */
    protected boolean strip;

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
     * Compression quality from 0.0 to 1.0
     * <p>
     * Equivalent to -q
     * </p>
     * 
     * @parameter expression="${flex.quality}"
     */
    private Double quality;

    /**
     * Strength of deblocking filter
     * <p>
     * Equivalent to -d
     * </p>
     * 
     * @parameter expression="${flex.deblock}"
     */
    private Double deblock;

    public abstract String getInput();

    public String[] getKeepAs3Metadata()
    {
        return keepAs3Metadatas;
    }

    public String[] getLoadConfig()
    {
        return PathUtil.getCanonicalPath( ConfigurationResolver.resolveConfiguration( loadConfigs, loadConfig,
                                                                                      configDirectory ) );
    }

    public IOptimizerConfiguration getOptimizerConfiguration( File input, File output )
    {
        // mocking real code doesn't seem to be a good idea, but produces a much cleaner code
        IOptimizerConfiguration cfg = mock( IOptimizerConfiguration.class, RETURNS_NULL );
        ICompilerConfiguration compilerCfg = mock( ICompilerConfiguration.class, RETURNS_NULL );
        when( cfg.getLoadConfig() ).thenReturn( getLoadConfig() );
        when( cfg.getInput() ).thenReturn( PathUtil.getCanonicalPath( input ) );
        when( cfg.getOutput() ).thenReturn( PathUtil.getCanonicalPath( output ) );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        when( compilerCfg.getKeepAs3Metadata() ).thenReturn( getKeepAs3Metadata() );
        return cfg;
    }

    public String getOutput()
    {
        return PathUtil.getCanonicalPath( new File( build.getDirectory(), build.getFinalName() + ".swf" ) );
    }

    protected void optimize( File input, File output )
        throws MojoFailureException, MojoExecutionException
    {
        int result;
        try
        {
            result = compiler.optimize( getOptimizerConfiguration( input, output ), true ).getExitCode();
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

    protected void reduce( File input, File output )
    {
        ReducerTool s = new ReducerTool();
        ApparatConfiguration cfg = new ApparatConfiguration();
        cfg.update( "-i", PathUtil.getCanonicalPath( input ) );
        cfg.update( "-o", PathUtil.getCanonicalPath( output ) );

        if ( quality != null )
        {
            cfg.update( "-q", String.valueOf( quality ) );
        }

        if ( deblock != null )
        {
            cfg.update( "-d", String.valueOf( deblock ) );
        }

        s.configure( cfg );
        s.run();
    }

    protected void strip( File input, File output )
    {
        StripperTool s = new StripperTool();
        ApparatConfiguration cfg = new ApparatConfiguration();
        cfg.update( "-i", PathUtil.getCanonicalPath( input ) );
        cfg.update( "-o", PathUtil.getCanonicalPath( output ) );
        s.configure( cfg );
        s.run();
    }

    protected File optimize()
        throws MojoFailureException, MojoExecutionException
    {
        File input = PathUtil.getCanonicalFile( getInput() );
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
            final File output = PathUtil.getCanonicalFile( getOutput() );
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

}