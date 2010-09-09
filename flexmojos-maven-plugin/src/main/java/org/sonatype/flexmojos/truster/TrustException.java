package org.sonatype.flexmojos.truster;

public class TrustException
    extends RuntimeException
{

    private static final long serialVersionUID = 3938690139697545739L;

    public TrustException( String msg, Exception cause )
    {
        super( msg, cause );
    }

}
