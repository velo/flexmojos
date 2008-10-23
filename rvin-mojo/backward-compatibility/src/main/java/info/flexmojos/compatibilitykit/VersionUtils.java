/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.compatibilitykit;

public class VersionUtils
{

    public static int[] splitVersion( String version )
    {
        if ( version == null || version.trim().equals( "" ) )
        {
            return new int[0];
        }

        int endIndex = version.indexOf( '-' );
        if ( endIndex != -1 )
        {
            version = version.substring( 0, endIndex );
        }

        String[] versionsStr = version.split( "\\." );
        int[] versions = new int[versionsStr.length];

        for ( int i = 0; i < versionsStr.length; i++ )
        {
            versions[i] = new Integer( versionsStr[i] );
        }

        return versions;
    }

    public static boolean isMinVersionOK( int[] fdkVersion, int[] minVersion )
    {
        int lenght = getSmaller( fdkVersion.length, minVersion.length );

        for ( int i = 0; i < lenght; i++ )
        {
            int min = minVersion[i];
            int current = fdkVersion[i];
            if ( current < min )
            {
                return false;
            }
        }

        return true;
    }

    public static boolean isMaxVersionOK( int[] fdkVersion, int[] maxVersion )
    {
        int lenght = getSmaller( fdkVersion.length, maxVersion.length );

        for ( int i = 0; i < lenght; i++ )
        {
            int max = maxVersion[i];
            int current = fdkVersion[i];
            if ( current > max )
            {
                return false;
            }
        }

        return true;
    }

    private static int getSmaller( int... integers )
    {
        if ( integers.length == 0 )
        {
            return 0;
        }

        int smaller = Integer.MAX_VALUE;

        for ( int integer : integers )
        {
            if ( integer < smaller )
            {
                smaller = integer;
            }
        }

        return smaller;
    }

}
