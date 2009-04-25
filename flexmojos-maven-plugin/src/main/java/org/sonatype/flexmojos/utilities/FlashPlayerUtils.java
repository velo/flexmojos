/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.utilities;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

public class FlashPlayerUtils
{

    /**
     * Retrieves flash player trust folder, based on:
     * http://livedocs.adobe.com/flex/3/html/help.html?content=05B_Security_03.html #140756
     */
    public static File getTrustDir()
        throws MojoExecutionException
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
            throw new MojoExecutionException( "Unable to resolve current OS: " + MavenUtils.osString() );
        }

        File trustDir = new File( trustPath );
        if ( !trustDir.exists() )
        {
            trustDir.mkdirs();
        }

        return trustDir;

    }

}
