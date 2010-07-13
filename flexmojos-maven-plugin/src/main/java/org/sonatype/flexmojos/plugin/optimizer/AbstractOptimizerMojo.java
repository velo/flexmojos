/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.plugin.optimizer;

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
import org.sonatype.flexmojos.compiler.FlexCompiler;
import org.sonatype.flexmojos.compiler.ICompilerConfiguration;
import org.sonatype.flexmojos.compiler.IOptimizerConfiguration;
import org.sonatype.flexmojos.plugin.AbstractMavenMojo;
import org.sonatype.flexmojos.plugin.utilities.ConfigurationResolver;
import org.sonatype.flexmojos.util.PathUtil;

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
    private float quality;

    /**
     * Strength of deblocking filter
     * <p>
     * Equivalent to -d
     * </p>
     * 
     * @parameter expression="${flex.deblock}"
     */
    private float deblock;
    
    /**
     * Instructs apparat to whether or not to merge ABC files into a single one.
     * <p>
     * Equivalent to -d
     * </p>
     * 
     * @parameter expression="${flex.mergeABC}" default-value="true"
     */
    private boolean mergeABC;

    public abstract String getInput();

    public String[] getKeepAs3Metadata()
    {
        return keepAs3Metadatas;
    }

    public String[] getLoadConfig()
    {
        return PathUtil.getCanonicalPaths( ConfigurationResolver.resolveConfiguration( loadConfigs, loadConfig,
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

    protected void reduce( final File input, final File output )
    {
        ReducerTool s = new ReducerTool();
        ReducerConfiguration cfg = new ReducerConfiguration()
        {

            public float quality()
            {
                return quality;
            }

            public File output()
            {
                return output;
            }

            public File input()
            {
                return input;
            }

            public float deblock()
            {
                return deblock;
            }
            
            public boolean mergeABC()
            {
                return mergeABC;
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
            public File output()
            {
                return output;
            }

            public File input()
            {
                return input;
            }
        };
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