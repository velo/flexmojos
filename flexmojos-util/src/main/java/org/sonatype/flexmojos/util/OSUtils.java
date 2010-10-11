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
package org.sonatype.flexmojos.util;

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

    public static String[] getPlatformDefaultFlashPlayer()
    {
        switch ( getOSType() )
        {
            case windows:
                return new String[] { WINDOWS_CMD };
            case mac:
                return new String[] { MAC_CMD };
            default:
                return new String[] { UNIX_CMD };
        }
    }

    public static String[] getPlatformDefaultAdl()
    {
        switch ( getOSType() )
        {
            case windows:
                return new String[] { "adl.exe" };
            default:
                return new String[] { "adl" };
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

    public static boolean isWindows()
    {
        return getOSType().equals( OS.windows );
    }

    public static boolean isMacOS()
    {
        return getOSType().equals( OS.mac );
    }

}