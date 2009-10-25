package org.sonatype.flexmojos.compiler.command;

public class Result
{
    private Exception exception;

    private int exitCode;

    public int getExitCode()
    {
        return exitCode;
    }

    public void setExitCode( int exitCode )
    {
        this.exitCode = exitCode;
    }

    public Exception getException()
    {
        return exception;
    }

    public void setException( Exception exception )
    {
        this.exception = exception;
    }
}
