/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.flexmojos.oss.plugin.air.packager;

import com.adobe.air.AIRPackager;
import com.adobe.air.Listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class FlexmojosAIRPackager
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
        throws GeneralSecurityException, IOException
    {
        try
        {
            this.packager.createPackage();
        }
        catch ( NoSuchMethodError ex )
        {
            try
            {
                Class<? extends AIRPackager> packagerClass = this.packager.getClass();
                Method packagerMethod = packagerClass.getMethod( "createAIR" );
                packagerMethod.invoke( this.packager );
            }
            catch ( Exception e )
            {
                throw new IllegalStateException(e);
            }
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
