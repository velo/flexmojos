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
package org.sonatype.flexmojos.plugin.air.packager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import com.adobe.air.Listener;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public interface IPackager
{

    void addSourceWithPath( File source, String path );

    void close();

    void createPackage()
        throws GeneralSecurityException, IOException, MojoExecutionException, MojoFailureException;

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
