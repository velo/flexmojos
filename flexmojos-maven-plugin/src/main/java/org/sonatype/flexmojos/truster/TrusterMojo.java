package org.sonatype.flexmojos.truster;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Write a file entry onto flashplayer trust file
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.3
 * @goal trust
 * @phase process-resources
 * @threadSafe
 */
public class TrusterMojo
    extends AbstractMojo
{

    /**
     * @component
     */
    private FlashPlayerTruster truster;

    /**
     * Files that should be included on flashplayer trust
     * 
     * @parameter
     * @required
     */
    private File[] filesToTrust;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        for ( File fileToTrust : filesToTrust )
        {
            try
            {
                truster.updateSecuritySandbox( fileToTrust );
            }
            catch ( TrustException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
    }

}
