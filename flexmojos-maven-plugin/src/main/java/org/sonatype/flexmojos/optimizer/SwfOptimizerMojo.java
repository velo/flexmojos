package org.sonatype.flexmojos.optimizer;

import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.common.MavenRuntimeException;
import org.sonatype.flexmojos.test.util.PathUtil;

/**
 * Goal which run post-link SWF optimization on swc files. This goal is used to produce RSL files.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 2.0
 * @goal optimize-swf
 * @phase package
 */
public class SwfOptimizerMojo
    extends AbstractOptimizerMojo
{

    /**
     * Defines whether the original artifact should be attached as classifier to the optimized artifact. If false, the
     * optimized swf will be the main artifact of the project
     * 
     * @parameter default-value="true" expression="${flex.attachOriginalArtifact}"
     */
    private boolean attachOriginalArtifact;

    /**
     * The name of the classifier used in case the original artifact is attached.
     * 
     * @parameter default-value="original" expression="${flex.originalClassifierName}"
     */
    private String originalClassifierName;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().debug( "project.getPackaging = " + packaging );

        if ( !SWF.equals( packaging ) )
        {
            getLog().warn( "Swf optimizer goal can only be used on SWF projects." );
            return;
        }

        try
        {
            compiler.optimize( getOptimizerConfiguration() , true);
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

    }

    @Override
    public String getInput()
    {
        File originalFile = project.getArtifact().getFile();

        getLog().debug( "attempting to optimize: " + originalFile.getName() );

        File bkpOriginalFile = getOriginalSwfFile();

        try
        {
            FileUtils.copyFile( originalFile, bkpOriginalFile );
        }
        catch ( IOException e )
        {
            throw new MavenRuntimeException( "Unable to backup SWF file.", e );
        }

        getLog().debug( "attaching original swf" );
        if ( attachOriginalArtifact )
        {
            projectHelper.attachArtifact( project, SWF, originalClassifierName, bkpOriginalFile );
        }

        return PathUtil.getCanonicalPath( bkpOriginalFile );
    }

    protected File getOriginalSwfFile()
    {
        File bkpOriginalFile =
            new File( build.getDirectory(), build.getFinalName() + "-" + originalClassifierName + ".swf" );
        return bkpOriginalFile;
    }
}
