package org.sonatype.flexmojos.test.threads;

public class ControlledThreadUtil
{
    public static boolean hasDone( ControlledThread... threads )
    {
        for ( ControlledThread controlledThread : threads )
        {
            if ( ThreadStatus.DONE.equals( controlledThread.getStatus() ) )
            {
                return true;
            }
        }
        return false;
    }

    public static Error getError( ControlledThread... threads )
    {
        for ( ControlledThread controlledThread : threads )
        {
            if ( controlledThread.getError() != null )
            {
                return controlledThread.getError();
            }
        }

        throw new IllegalStateException( "No error found!" );
    }

    public static void stop( ControlledThread... threads )
    {
        for ( ControlledThread controlledThread : threads )
        {
            if ( controlledThread != null )
            {
                controlledThread.stop();
            }
        }
    }

    public static boolean hasError( ControlledThread... threads )
    {
        for ( ControlledThread controlledThread : threads )
        {
            if ( ThreadStatus.ERROR.equals( controlledThread.getStatus() ) )
            {
                return true;
            }
        }
        return false;
    }
}
