package info.flexmojos.utilities;

import info.rvin.flexmojos.utilities.MavenUtils;

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
            if ( MavenUtils.isWindowsVista() )
            {
                trustPath = appData + "/Roaming/Macromedia/Flash Player/#Security/FlashPlayerTrust";
            }
            else
            {
                trustPath = appData + "/Macromedia/Flash Player/#Security/FlashPlayerTrust";
            }
        }
        else if ( MavenUtils.isUnixBased() )
        {
            trustPath = home + ".macromedia/Flash_Player/#Security/FlashPlayerTrust";
        }
        else if ( MavenUtils.isMac() )
        {
            trustPath = home + "Library/Preferences/Macromedia/Flash Player/#Security/FlashPlayerTrust";
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
