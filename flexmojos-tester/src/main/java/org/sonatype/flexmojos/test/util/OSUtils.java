package org.sonatype.flexmojos.test.util;

public class OSUtils
{

    private static final String WINDOWS_CMD = "FlashPlayer.exe";

    private static final String MAC_CMD = "Flash Player";

    private static final String UNIX_CMD = "flashplayer";

    public enum OS
    {
        windows, linux, solaris, mac, unix, other;
    }

    public static OS getOSType()
    {
        String osName = System.getProperty( "os.name" ).toLowerCase();
        for ( OS os : OS.values() )
        {
            if ( osName.contains( os.toString() ) )
            {
                return os;
            }
        }
        return OS.other;
    }

    public static String getPlatformDefaultCommand()
    {
        switch ( getOSType() )
        {
            case windows:
                return WINDOWS_CMD;
            case mac:
                return MAC_CMD;
            default:
                return UNIX_CMD;
        }
    }

    public static boolean isLinux()
    {
        switch ( getOSType() )
        {
            case windows:
            case mac:
                return false;
            default:
                return true;
        }
    }

}