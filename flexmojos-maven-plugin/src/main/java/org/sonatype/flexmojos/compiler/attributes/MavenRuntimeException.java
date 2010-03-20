package org.sonatype.flexmojos.compiler.attributes;

public class MavenRuntimeException
    extends RuntimeException
{

    public MavenRuntimeException( Throwable e )
    {
        super( e );
    }

    public MavenRuntimeException( String message )
    {
        super( message );
    }

    public MavenRuntimeException( String msg, Throwable e )
    {
        super( msg, e );
    }

    private static final long serialVersionUID = -9121873468164622710L;

}
