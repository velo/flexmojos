package org.sonatype.flexmojos.plugin.air.packager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import com.adobe.air.Listener;

public interface IPackager
{

    void addSourceWithPath( File source, String path );

    void close();

    void createPackage()
        throws GeneralSecurityException, IOException;

    void setCertificateChain( Certificate[] certificateChain )
        throws CertificateException;

    void setDescriptor( File airDescriptor );

    void setListener( Listener listener );

    void setOutput( File output )
        throws FileNotFoundException, IOException;

    void setPrivateKey( PrivateKey key );

    void setSignerCertificate( Certificate certificate )
        throws CertificateException;

    void setTimestampURL( String string );

}
