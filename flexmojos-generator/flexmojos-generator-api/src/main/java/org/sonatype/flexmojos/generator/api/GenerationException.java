package org.sonatype.flexmojos.generator.api;

public class GenerationException
    extends Exception
{

    private static final long serialVersionUID = -4101638383564443510L;

    public GenerationException()
    {
        super();
    }

    public GenerationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public GenerationException( String message )
    {
        super( message );
    }

}
