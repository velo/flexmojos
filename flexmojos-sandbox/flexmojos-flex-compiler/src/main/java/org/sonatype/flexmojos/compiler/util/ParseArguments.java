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

import org.sonatype.flexmojos.compiler.IFlexConfiguration;
import org.sonatype.flexmojos.generator.iface.StringUtil;

public class ParseArguments
{
    public static <E> List<String> getArguments( E cfg, Class<? extends E> configClass )
    {
        List<CharSequence> charArgs = doGetArgs( cfg, configClass );
        List<String> args = new ArrayList<String>();
        for ( CharSequence charSequence : charArgs )
        {
            args.add( "-" + charSequence );
        }
        return args;
    }

    private static <E> List<CharSequence> doGetArgs( E cfg, Class<? extends E> configClass )
    {
        if ( cfg == null )
        {
            return Collections.emptyList();
        }

        List<CharSequence> args = new ArrayList<CharSequence>();

        Method[] methods = configClass.getDeclaredMethods();
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

            if ( value instanceof IFlexConfiguration )
            {
                List<CharSequence> subArgs = doGetArgs( value, method.getReturnType() );
                String configurationName = parseConfigurationName( method.getName() );
                for ( CharSequence arg : subArgs )
                {
                    args.add( configurationName + "." + arg );
                }
            }
            else
            {
                args.add( parseName( method.getName() ) + "=" + value.toString() );
            }

        }
        return args;
    }

    private static String parseConfigurationName( String name )
    {
        name = parseName( name );
        name = name.substring( 0, name.length() - 14 );
        return name;
    }

    private static String parseName( String name )
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

        return finalName.toString();
    }
}
