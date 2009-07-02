package org.sonatype.flexmojos.test.launcher;

public class InvalidSwfException
    extends IllegalArgumentException
{

    private static final long serialVersionUID = 5043689829769347687L;

    public InvalidSwfException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public InvalidSwfException( String s )
    {
        this( s, null );
    }

}
