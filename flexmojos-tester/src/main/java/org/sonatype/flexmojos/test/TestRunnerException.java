package org.sonatype.flexmojos.test;

public class TestRunnerException
    extends Exception
{

    private static final long serialVersionUID = 6504362216993363359L;

    public TestRunnerException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public TestRunnerException( String message )
    {
        super( message );
    }

}
