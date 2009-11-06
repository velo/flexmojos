package org.sonatype.flexmojos.test.launcher;

public class FlashPlayerShutdownHook
    extends Thread
{

    private Process process;

    public FlashPlayerShutdownHook( Process process )
    {
        this.process = process;
    }

    @Override
    public void run()
    {
        if ( process != null )
        {
            try
            {
                process.exitValue();
            }
            catch ( IllegalThreadStateException e )
            {
                // it didn't finished by itself, my turn then =D
                process.destroy();
            }
        }
    }

}
