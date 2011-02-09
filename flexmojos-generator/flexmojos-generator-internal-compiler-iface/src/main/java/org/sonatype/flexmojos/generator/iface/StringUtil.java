package org.sonatype.flexmojos.generator.iface;

import java.util.ArrayList;
import java.util.List;

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

    public static String[] splitCamelCase( String string )
    {
        List<String> nodes = new ArrayList<String>();

        char[] chars = string.toCharArray();

        StringBuilder buff = new StringBuilder();
        for ( int i = 0; i < chars.length; i++ )
        {
            char c = chars[i];
            if ( Character.isUpperCase( c ) )
            {
                nodes.add( buff.toString() );
                buff.delete( 0, buff.length() );
                buff.append( Character.toLowerCase( c ) );
            }
            else
            {
                buff.append( c );
            }
        }

        if ( buff.length() != 0 )
        {
            nodes.add( buff.toString() );
        }

        return nodes.toArray( new String[nodes.size()] );
    }

    public static String toUpperCamelCase( String name )
    {
        String[] texts = splitCamelCase( name );

        StringBuilder sb = new StringBuilder();
        for ( String text : texts )
        {
            if ( sb.length() != 0 )
            {
                sb.append( '_' );
            }
            sb.append( text.toUpperCase() );
        }

        return sb.toString();
    }

}
