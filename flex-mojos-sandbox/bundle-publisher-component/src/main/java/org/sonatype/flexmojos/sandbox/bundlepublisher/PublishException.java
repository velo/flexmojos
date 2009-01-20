package org.sonatype.flexmojos.sandbox.bundlepublisher;

public class PublishException
    extends Exception
{

    private static final long serialVersionUID = -7642151332926657427L;

    public PublishException()
    {
        this( null );
    }

    public PublishException( String message )
    {
        this( message, null );
    }

    public PublishException( String message, Throwable cause )
    {
        super( message, cause );
    }

}
