package org.sonatype.flexmojos.compiler.command;

public class Result
{
    private Exception exception;

    private int exitCode;

    private Thread thread;

    public int getExitCode()
        throws Exception
    {
        checkException();

        return exitCode;
    }

    public void setExitCode( int exitCode )
    {
        this.exitCode = exitCode;
    }

    public void checkException()
        throws Exception
    {
        thread.join();

        if ( exception != null )
        {
            throw exception;
        }
    }

    public void setException( Exception exception )
    {
        this.exception = exception;
    }

    public void setThread( Thread t )
    {
        this.thread = t;
    }
}
