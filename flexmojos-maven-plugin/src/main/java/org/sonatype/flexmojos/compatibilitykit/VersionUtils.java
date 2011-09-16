/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.compatibilitykit;

import static java.lang.Math.min;

import java.util.Arrays;

public class VersionUtils
{

    public static boolean isMaxVersionOK( int[] fdkVersion, int[] maxVersion )
    {
        return isVersionOK( maxVersion, fdkVersion );
    }

    public static boolean isMaxVersionOK( String fdkVersion, String maxVersion )
    {
        return isMaxVersionOK( splitVersion( fdkVersion ), splitVersion( maxVersion ) );
    }

    public static boolean isMinVersionOK( int[] fdkVersion, int[] minVersion )
    {
        return isVersionOK( fdkVersion, minVersion );
    }

    public static boolean isMinVersionOK( String fdkVersion, String minVersion )
    {
        return isVersionOK( splitVersion( fdkVersion ), splitVersion( minVersion ) );
    }

    private static boolean isVersionOK( int[] fdkVersion, int[] minVersion )
    {
        int lenght = min( fdkVersion.length, minVersion.length );

        int result = 0;
        for ( int i = 0; i < lenght; i++ )
        {
            result = fdkVersion[i] - minVersion[i];
            if ( result != 0 )
            {
                return result > -1;
            }
        }

        return result > -1;
    }

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
            try
            {
                versions[i] = new Integer( versionsStr[i] );
            }
            catch ( NumberFormatException e )
            {
                versions[i] = 0;
            }
        }

        return versions;
    }

    public static int[] splitVersion( String version, int size )
    {
        int[] versions = splitVersion( version );

        if ( versions.length != size )
        {
            int[] temp = new int[size];
            Arrays.fill( temp, 0 );
            System.arraycopy( versions, 0, temp, 0, Math.min( versions.length, size ) );
            versions = temp;
        }

        return versions;
    }

}
