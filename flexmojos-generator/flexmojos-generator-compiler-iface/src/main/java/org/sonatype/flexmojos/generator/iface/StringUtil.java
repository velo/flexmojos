package org.sonatype.flexmojos.generator.iface;


public class StringUtil
{

    public static String removePrefix( String str )
    {
        int cut = 0;
        for ( int i = 0; i < str.toCharArray().length; i++ )
        {
            char c = str.toCharArray()[i];
            if ( Character.isUpperCase( c ) )
            {
                cut = i;
                break;
            }
        }

        return str.substring( cut );
    }

    public static String toCamelCase( String str )
    {
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        boolean nextUp = false;
        for ( int i = 0; i < chars.length; i++ )
        {
            char c = chars[i];
            if ( '-' == c )
            {
                nextUp = true;
                continue;
            }

            if ( nextUp )
            {
                c = Character.toUpperCase( c );
                nextUp = false;
            }

            sb.append( c );
        }

        return sb.toString();
    }

}
