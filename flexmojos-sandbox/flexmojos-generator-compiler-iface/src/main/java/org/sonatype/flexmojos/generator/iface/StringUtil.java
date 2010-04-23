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
