package org.sonatype.flexmojos.plugin.air.packager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import com.adobe.air.AIRPackager;
import com.adobe.air.Listener;

public class FlexmojosAIRPackager
    implements IPackager
{

    private final AIRPackager packager;

    public FlexmojosAIRPackager()
    {
        super();
        this.packager = new AIRPackager();
    }

    public void setOutput( File output )
        throws FileNotFoundException, IOException
    {
        this.packager.setOutput( output );
    }

    public void setDescriptor( File airDescriptor )
    {
        this.packager.setDescriptor( airDescriptor );
    }

    public void setPrivateKey( PrivateKey key )
    {
        this.packager.setPrivateKey( key );
    }

    public void setSignerCertificate( Certificate certificate )
        throws CertificateException
    {
        this.packager.setSignerCertificate( certificate );
    }

    public void setCertificateChain( Certificate[] certificateChain )
        throws CertificateException
    {
        this.packager.setCertificateChain( certificateChain );
    }

    public void setTimestampURL( String url )
    {
        this.packager.setTimestampURL( url );
    }

    public void addSourceWithPath( File source, String path )
    {
        this.packager.addSourceWithPath( source, path );
    }

    public void setListener( Listener listener )
    {
        this.packager.setListener( listener );
    }

    public void createPackage()
        throws GeneralSecurityException, IOException
    {
        this.packager.createPackage();
    }

    public void close()
    {
        this.packager.close();
    }

}
