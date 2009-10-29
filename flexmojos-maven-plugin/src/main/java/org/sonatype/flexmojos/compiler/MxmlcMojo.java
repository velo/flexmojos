package org.sonatype.flexmojos.compiler;

import java.io.File;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.common.AbstractMavenFlexCompilerConfiguration;
import org.sonatype.flexmojos.utilities.SourceFileResolver;

public class MxmlcMojo
    extends AbstractMavenFlexCompilerConfiguration
    implements ICommandLineConfiguration
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
     * @parameter
     */
    private String sourceFile;

    public List<String> getFileSpecs()
    {
        return fileSpecs;
    }

    public String getProjector()
    {
        return projector;
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            compiler.compileSwf( this, getSourceFile() );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    protected File getSourceFile()
    {
        return SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), sourceFile, project.getGroupId(),
                                                     project.getArtifactId() );
    }
}
