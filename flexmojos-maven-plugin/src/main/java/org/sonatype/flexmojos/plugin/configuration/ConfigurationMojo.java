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
package org.sonatype.flexmojos.plugin.configuration;

import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;

import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.configurator.Configurator;
import org.sonatype.flexmojos.plugin.compiler.CompcMojo;

/**
 * <p>
 * Goal used as an extension point, by itself it is useless
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal configurator
 * @requiresDependencyResolution compile
 * @configurator flexmojos
 * @threadSafe
 */
public class ConfigurationMojo
    extends CompcMojo
    implements ICommandLineConfiguration
{

    /**
     * @parameter expression="${flex.configurator}"
     */
    private String configurator;

    /**
     * @component
     * @readonly
     */
    private Map<String, Configurator> configurators;

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
     * DOCME Another, undocumented by adobe
     * <p>
     * Equivalent to -projector
     * </p>
     * 
     * @parameter expression="${flex.projector}"
     */
    private String projector;

    @Override
    public Result doCompile( ICompcConfiguration cfg, boolean synchronize )
        throws Exception
    {
        throw new UnsupportedOperationException( "This is not a compilation mojo" );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Configurator cfg = configurators.get( configurator );
        if ( cfg == null )
        {
            // not a plexus component, trying brute force
            Class<Configurator> cfgClass;
            try
            {
                cfgClass = (Class<Configurator>) Class.forName( configurator );
            }
            catch ( ClassNotFoundException e )
            {
                throw new MojoExecutionException( "Configurator not found: " + configurator, e );
            }

            try
            {
                cfg = cfgClass.newInstance();
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "Erro creating a new configurator instance: " + configurator, e );
            }
        }

        if ( SWC.equals( getProjectType() ) )
        {
            cfg.buildConfiguration( (ICompcConfiguration) this );
        }
        else
        {
            cfg.buildConfiguration( (ICommandLineConfiguration) this );
        }
    }

    public List getFileSpecs()
    {
        return fileSpecs;
    }

    public String getProjector()
    {
        return projector;
    }

    @Override
    public String getProjectType()
    {
        return packaging;
    }

}
