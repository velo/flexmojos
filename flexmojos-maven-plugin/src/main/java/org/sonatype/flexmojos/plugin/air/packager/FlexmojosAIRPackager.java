package org.sonatype.flexmojos.plugin.air.packager;

import com.adobe.air.AIRPackager;
import com.adobe.air.Listener;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class FlexmojosAIRPackager
    implements IPackager
{

    private final AIRPackager packager;

    public FlexmojosAIRPackager()
    {
        super();
        this.packager = new AIRPackager();
    }

    public void addSourceWithPath( File source, String path )
    {
        this.packager.addSourceWithPath( source, path );
    }

    public void close()
    {
        this.packager.close();
    }

    public void createPackage()
        throws GeneralSecurityException, IOException, MojoExecutionException, MojoFailureException
    {
        Class<? extends AIRPackager> packagerClass = this.packager.getClass();
        Method packagerMethod = null;

        try
        {
            packagerMethod = packagerClass.getMethod( "createPackage" );
        }
        catch ( NoSuchMethodException e )
        {
            // ignore
        }

        try
        {
            packagerMethod = packagerClass.getMethod( "createAIR" );
        }
        catch ( NoSuchMethodException e )
        {
            // ignore
        }

        if ( packagerMethod == null )
        {
            throw new MojoFailureException(
                "Unable to locate the method in AIRPackager to create the package, tried createPackage and createAIR." );
        }

        try
        {
            packagerMethod.invoke( this.packager );
        }
        catch ( IllegalAccessException e )
        {
            throw new MojoExecutionException( "Error invoking AIR API to create package.", e );
        }
        catch ( InvocationTargetException e )
        {
            throw new MojoExecutionException( "Error invoking AIR API to create package.", e );
        }
    }

    public void setCertificateChain( Certificate[] certificateChain )
        throws CertificateException
    {
        this.packager.setCertificateChain( certificateChain );
    }

    public void setDescriptor( File airDescriptor )
    {
        this.packager.setDescriptor( airDescriptor );
    }

    public void setListener( Listener listener )
    {
        this.packager.setListener( listener );
    }

    public void setOutput( File output )
        throws FileNotFoundException, IOException
    {
        this.packager.setOutput( output );
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

    public void setTimestampURL( String url )
    {
        this.packager.setTimestampURL( url );
    }

}
