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

}
