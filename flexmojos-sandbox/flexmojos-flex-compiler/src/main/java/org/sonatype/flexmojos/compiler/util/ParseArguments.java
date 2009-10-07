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
package org.sonatype.flexmojos.compiler.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.flexmojos.generator.iface.StringUtil;

public class ParseArguments
{
    public static List<String> getArguments( Object cfg )
    {
        if ( cfg == null )
        {
            return Collections.emptyList();
        }

        List<String> args = new ArrayList<String>();

        Method[] methods = cfg.getClass().getDeclaredMethods();
        for ( Method method : methods )
        {
            if ( method.getParameterTypes().length != 0 || !Modifier.isPublic( method.getModifiers() ) )
            {
                continue;
            }

            Object value;
            try
            {
                value = method.invoke( cfg );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }

            if ( value == null )
            {
                continue;
            }

            args.add( parseName( method.getName() ) + "=" + value.toString() );

        }
        return args;
    }

    private static CharSequence parseName( String name )
    {
        name = StringUtil.removePrefix( name );
        String[] nodes = StringUtil.splitCamelCase( name );

        StringBuilder finalName = new StringBuilder();
        for ( String node : nodes )
        {
            if ( finalName.length() != 0 )
            {
                finalName.append( '-' );
            }
            finalName.append( node.toLowerCase() );
        }

        return finalName;
    }
}
