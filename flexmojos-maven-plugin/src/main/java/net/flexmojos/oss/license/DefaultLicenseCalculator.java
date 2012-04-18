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
package net.flexmojos.oss.license;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import net.flexmojos.oss.plugin.utilities.MavenUtils;

@Component( role = LicenseCalculator.class )
public class DefaultLicenseCalculator
    extends AbstractLogEnabled
    implements LicenseCalculator
{

    public Map<String, String> getInstalledLicenses()
    {
        File licensePropertyFile = getOSLicensePropertyFile();

        if ( licensePropertyFile == null || !licensePropertyFile.exists() )
        {
            return Collections.emptyMap();
        }

        Properties props = new Properties();
        try
        {
            props.load( new FileInputStream( licensePropertyFile ) );
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to read license file " + licensePropertyFile.getAbsolutePath(), e );
            return Collections.emptyMap();
        }

        Map<String, String> licenses = new HashMap<String, String>();

        Enumeration<?> names = props.propertyNames();
        while ( names.hasMoreElements() )
        {
            String name = (String) names.nextElement();
            String value = props.getProperty( name );
            licenses.put( name, value );
        }

        return licenses;
    }

    /**
     * license.properties locations get from http://livedocs.adobe.com/flex/3/html/configuring_environment_2.html
     */
    public File getOSLicensePropertyFile()
    {

        if ( MavenUtils.isWindows() )
        {
            return new File( // Windows XP
                             "C:/Documents and Settings/All Users/Application Data/Adobe/Flex/license.properties" );
        }

        if ( MavenUtils.isWindowsVista() )
        {
            return new File( // Windows Vista
                             "C:/ProgramData/Adobe/Flex/license.properties" );
        }

        if ( MavenUtils.isMac() )
        {
            return new File( // Mac OSX
                             "/Library/Application Support/Adobe/Flex/license.properties" );
        }

        if ( MavenUtils.isLinux() )
        {
            return new File( // Linux
                             System.getProperty( "user.home" ), ".adobe/Flex/license.properties" );
        }

        return null;
    }
}
