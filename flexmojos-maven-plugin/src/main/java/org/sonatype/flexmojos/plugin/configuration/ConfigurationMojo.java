package org.sonatype.flexmojos.plugin.configuration;

import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWF;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.compiler.command.Result;
import org.sonatype.flexmojos.configurator.Configurator;
import org.sonatype.flexmojos.configurator.ConfiguratorException;
import org.sonatype.flexmojos.plugin.compiler.CompcMojo;
import org.sonatype.flexmojos.plugin.utilities.SourceFileResolver;

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

    /**
     * Extra parameters that will be injected onto configurator
     * 
     * <pre>
     * &lt;parameters&gt;
     *   &lt;extra&gt;???&lt;/extra&gt;
     *   &lt;plus&gt;???&lt;/plus&gt;
     * &lt;/parameters&gt;
     * </pre>
     * 
     * @parameters
     */
    private Map<String, Object> parameters;

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
        if ( !( packaging.equals( SWC ) || packaging.equals( SWF ) ) )
        {
            return;
        }

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

        if ( parameters == null )
        {
            parameters = new LinkedHashMap<String, Object>();
        }
        parameters.put( "project", project );
        parameters.put( "classifier", classifier );

        try
        {
            if ( SWC.equals( getProjectType() ) )
            {
                cfg.buildConfiguration( (ICompcConfiguration) this, parameters );
            }
            else
            {
                cfg.buildConfiguration( (ICommandLineConfiguration) this, getSourceFile(), parameters );
            }
        }
        catch ( ConfiguratorException e )
        {
            throw new MojoExecutionException( "Failed to execute configurator: " + e.getMessage(), e );
        }
    }

    public List<String> getFileSpecs()
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

    /**
     * The file to be compiled. The path must be relative with source folder
     * 
     * @parameter expression="${flex.sourceFile}"
     */
    private String sourceFile;

    protected File getSourceFile()
    {
        return SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), sourceFile, project.getGroupId(),
                                                     project.getArtifactId() );
    }

}
