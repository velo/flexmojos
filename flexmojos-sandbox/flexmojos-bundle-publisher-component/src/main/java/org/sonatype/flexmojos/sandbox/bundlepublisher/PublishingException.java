package org.sonatype.flexmojos.sandbox.bundlepublisher;

public class PublishingException
    extends Exception
{

    private static final long serialVersionUID = -7642151332926657427L;

    public PublishingException()
    {
        this( null );
    }

    public PublishingException( String message )
    {
        this( message, null );
    }

    public PublishingException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
