package org.sonatype.flexmojos.plugin.compiler.continuous;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.plugin.compiler.CompcMojo;

/**
 * @since 4.0
 * @goal cc-swc
 * @requiresDependencyResolution compile
 * @phase compile
 * @threadSafe
 * @author Joa Ebert
 * @requiresDirectInvocation
 */
public class CompcContinuousCompileMojo
    extends CompcMojo
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        //
        // We have ot set quick to true since isCompilationRequired does a check
        // only if quick has been set to true.
        //

        super.quick = true;

        try
        {
            showInfo();

            while ( !Thread.interrupted() )
            {
                if ( isCompilationRequired() )
                {
                    //
                    // We have to compile so let's hand the job to
                    // the CompcMojo implementation and do the actual work.
                    //

                    super.execute();

                    showInfo();

                    Thread.sleep( 4000L );
                }
                else
                {
                    Thread.sleep( 2000L );
                }
            }
        }
        catch ( final InterruptedException interruptException )
        {
            // nothing to do here
        }
    }

    protected void showInfo()
    {
        getLog().info( "Waiting for files to compile ..." );
    }
}
