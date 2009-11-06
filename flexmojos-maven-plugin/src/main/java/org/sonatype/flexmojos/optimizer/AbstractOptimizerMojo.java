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
package org.sonatype.flexmojos.optimizer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.mockito.ReturnValues;
import org.mockito.invocation.InvocationOnMock;
import org.sonatype.flexmojos.compiler.FlexCompiler;
import org.sonatype.flexmojos.compiler.ICompilerConfiguration;
import org.sonatype.flexmojos.compiler.IOptimizerConfiguration;
import org.sonatype.flexmojos.test.util.PathUtil;
import org.sonatype.flexmojos.utilities.ConfigurationResolver;

public abstract class AbstractOptimizerMojo
    extends AbstractMojo
{

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
     * Generates a movie that is suitable for debugging
     * <p>
     * Equivalent to -compiler.debug
     * </p>
     * 
     * @parameter expression="${flex.debug}"
     */
    protected Boolean debug;

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

    public Boolean getDebug()
    {
        return debug;
    }

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

    public IOptimizerConfiguration getOptimizerConfiguration()
    {
        // mocking real code doesn't seem to be a good idea, but produces a much cleaner code
        IOptimizerConfiguration cfg = mock( IOptimizerConfiguration.class, RETURNS_NULL );
        ICompilerConfiguration compilerCfg = mock( ICompilerConfiguration.class, RETURNS_NULL );
        when( cfg.getLoadConfig() ).thenReturn( getLoadConfig() );
        when( cfg.getInput() ).thenReturn( getInput() );
        when( cfg.getOutput() ).thenReturn( getOutput() );
        when( cfg.getCompilerConfiguration() ).thenReturn( compilerCfg );
        when( compilerCfg.getDebug() ).thenReturn( getDebug() );
        when( compilerCfg.getKeepAs3Metadata() ).thenReturn( getKeepAs3Metadata() );
        return cfg;
    }

    public String getOutput()
    {
        return PathUtil.getCanonicalPath( new File( build.getDirectory(), build.getFinalName() + ".swf" ) );
    }

}