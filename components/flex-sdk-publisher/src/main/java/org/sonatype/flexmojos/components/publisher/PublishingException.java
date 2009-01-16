package org.sonatype.flexmojos.components.publisher;

public class PublishingException
    extends Exception
{

    private static final long serialVersionUID = 4622375202301352416L;

    public PublishingException()
    {
        this( null );
    }

    public PublishingException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public PublishingException( String message )
    {
        this( message, null );
    }

}
