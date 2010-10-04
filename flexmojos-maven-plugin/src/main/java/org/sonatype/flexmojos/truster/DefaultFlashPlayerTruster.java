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
package org.sonatype.flexmojos.truster;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.flexmojos.plugin.utilities.MavenUtils;
import org.sonatype.flexmojos.util.PathUtil;

@Component( role = FlashPlayerTruster.class )
public class DefaultFlashPlayerTruster
    extends AbstractLogEnabled
    implements FlashPlayerTruster
{

    public void updateSecuritySandbox( File trustedFile )
        throws TrustException
    {

        String trustedPath = PathUtil.path( trustedFile.getParentFile() );

        File mavenCfg = new File( getTrustDir(), "maven.cfg" );
        if ( !mavenCfg.exists() )
        {
            try
            {
                // noinspection ResultOfMethodCallIgnored
                mavenCfg.createNewFile();
            }
            catch ( IOException e )
            {
                throw new TrustException( "Unable to create FlashPayerTrust file: " + mavenCfg.getAbsolutePath(), e );
            }
        }

        getLogger().debug( "maven.cfg location: " + mavenCfg );

        try
        {
            // Load maven.cfg
            FileReader input = new FileReader( mavenCfg );
            String text = IOUtils.toString( input );
            List<String> cfg = Arrays.asList( text.split( "\n" ) );
            input.close();

            if ( cfg.contains( trustedPath ) )
            {
                getLogger().debug( "Already trust on " + trustedPath );
                return;
            }
            else
            {
                getLogger().info( "Updating Flash Player Trust directory " + trustedPath );
            }

            if ( !text.endsWith( "\n" ) )
            {
                text = text + '\n';
            }

            // add builder folder
            text = text + trustedPath + '\n';

            // Save maven.cfg
            FileWriter output = new FileWriter( mavenCfg );
            IOUtils.write( text, output );
            output.flush();
            output.close();

        }
        catch ( IOException e )
        {
            throw new TrustException( "Unable to edit FlashPayerTrust file: " + mavenCfg.getAbsolutePath(), e );
        }
    }

    /**
     * Retrieves flash player trust folder, based on:
     * http://livedocs.adobe.com/flex/3/html/help.html?content=05B_Security_03.html #140756
     */
    public File getTrustDir()
    {
        String trustPath;
        String home = System.getProperty( "user.home" );

        if ( MavenUtils.isWindows() )
        {
            // workaround, application data folder is localized
            String appData = System.getenv( "APPDATA" );

            // use default english folders when APPDATA is not defined
            if ( appData == null )
            {
                if ( MavenUtils.isWindowsVista() )
                {
                    appData = home + "/AppData/Roaming";
                }
                else
                {
                    appData = home + "/Application Data";
                }
            }

            trustPath = appData + "/Macromedia/Flash Player/#Security/FlashPlayerTrust";
        }
        else if ( MavenUtils.isUnixBased() )
        {
            trustPath = home + "/.macromedia/Flash_Player/#Security/FlashPlayerTrust";
        }
        else if ( MavenUtils.isMac() )
        {
            trustPath = home + "/Library/Preferences/Macromedia/Flash Player/#Security/FlashPlayerTrust";
        }
        else
        // if isUnsupported OS
        {
            throw new IllegalArgumentException( "Unable to resolve current OS: " + MavenUtils.osString() );
        }

        File trustDir = new File( trustPath );
        if ( !trustDir.exists() )
        {
            trustDir.mkdirs();
        }

        return trustDir;

    }
}
