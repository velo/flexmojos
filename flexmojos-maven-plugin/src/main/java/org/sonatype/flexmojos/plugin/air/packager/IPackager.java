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

    void setOutput( File output )
        throws FileNotFoundException, IOException;

    void setDescriptor( File airDescriptor );

    void setPrivateKey( PrivateKey key );

    void setSignerCertificate( Certificate certificate )
        throws CertificateException;

    void setCertificateChain( Certificate[] certificateChain )
        throws CertificateException;

    void setTimestampURL( String string );

    void addSourceWithPath( File source, String path );

    void setListener( Listener listener );

    void createPackage()
        throws GeneralSecurityException, IOException;

    void close();
}
