/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.truster;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.flexmojos.test.util.PathUtil;
import org.sonatype.flexmojos.utilities.MavenUtils;

@Component( role = FlashPlayerTruster.class )
public class DefaultFlashPlayerTruster
    extends AbstractLogEnabled
    implements FlashPlayerTruster
{

    public void updateSecuritySandbox( File trustedFile )
        throws TrustException
    {

        String trustedPath = PathUtil.getCanonicalPath( trustedFile );

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
            String cfg = IOUtils.toString( input );
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

            if ( !cfg.endsWith( "\n" ) )
            {
                cfg = cfg + '\n';
            }

            // add builder folder
            cfg = cfg + trustedPath + '\n';

            // Save maven.cfg
            FileWriter output = new FileWriter( mavenCfg );
            IOUtils.write( cfg, output );
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

            if ( MavenUtils.isWindowsVista() )
            {
                trustPath = appData + "/Macromedia/Flash Player/#Security/FlashPlayerTrust";
            }
            else
            {
                trustPath = appData + "/Macromedia/Flash Player/#Security/FlashPlayerTrust";
            }
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
