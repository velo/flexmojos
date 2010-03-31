package org.sonatype.flexmojos.license;

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
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;

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
