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