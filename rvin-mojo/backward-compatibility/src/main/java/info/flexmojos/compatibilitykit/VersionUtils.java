package info.flexmojos.compatibilitykit;

public class VersionUtils
{

    public static int[] splitVersion( String version )
    {
        if ( version == null || version.trim().equals( "" ) )
        {
            return new int[0];
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
