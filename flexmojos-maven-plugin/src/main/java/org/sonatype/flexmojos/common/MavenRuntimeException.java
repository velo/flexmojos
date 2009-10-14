package org.sonatype.flexmojos.common;


public class MavenRuntimeException
    extends RuntimeException
{

    public MavenRuntimeException( Throwable e )
    {
        super( e );
    }

    private static final long serialVersionUID = -9121873468164622710L;

}
