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
package org.sonatype.flexmojos.compiler.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.sonatype.flexmojos.compiler.IFlexArgument;
import org.sonatype.flexmojos.compiler.IFlexConfiguration;
import org.sonatype.flexmojos.generator.iface.StringUtil;

public class ParseArguments
{
    public static <E> List<String> getArguments( E cfg, Class<? extends E> configClass )
    {
        Set<CharSequence> charArgs;
        try
        {
            charArgs = doGetArgs( cfg, configClass );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }

        List<String> args = new ArrayList<String>();
        for ( CharSequence charSequence : charArgs )
        {
            args.add( "-" + charSequence );
        }
        return args;
    }

    private static <E> Set<CharSequence> doGetArgs( E cfg, Class<? extends E> configClass )
        throws Exception
    {
        if ( cfg == null )
        {
            return Collections.emptySet();
        }

        Set<CharSequence> args = new LinkedHashSet<CharSequence>();

        Method[] methods = configClass.getDeclaredMethods();
        for ( Method method : methods )
        {
            if ( method.getParameterTypes().length != 0 || !Modifier.isPublic( method.getModifiers() ) )
            {
                continue;
            }

            Object value = method.invoke( cfg );

            if ( value == null )
            {
                continue;
            }

            Class<?> returnType = method.getReturnType();

            if ( value instanceof IFlexConfiguration )
            {
                Set<CharSequence> subArgs = doGetArgs( value, returnType );
                String configurationName = parseConfigurationName( method.getName() );
                for ( CharSequence arg : subArgs )
                {
                    args.add( configurationName + "." + arg );
                }
            }
            else if ( value instanceof IFlexArgument || value instanceof IFlexArgument[] )
            {
                IFlexArgument[] values;
                Class<?> type = returnType;
                if ( type.isArray() )
                {
                    values = (IFlexArgument[]) value;
                    type = returnType.getComponentType();
                }
                else
                {
                    values = new IFlexArgument[] { (IFlexArgument) value };
                    type = returnType;
                }

                for ( IFlexArgument iFlexArgument : values )
                {
                    String[] order = (String[]) type.getField( "ORDER" ).get( iFlexArgument );
                    StringBuilder arg = new StringBuilder();
                    for ( String argMethodName : order )
                    {
                        if ( arg.length() != 0 )
                        {
                            arg.append( ' ' );
                        }

                        Object argValue = type.getDeclaredMethod( argMethodName ).invoke( iFlexArgument );

                        arg.append( argValue.toString() );
                    }

                    args.add( parseName( method.getName() ) + " " + arg );
                }
            }
            else if ( returnType.isArray() )
            {
                Object[] values = (Object[]) value;
                String name = parseName( method.getName() );
                if ( values.length == 0 )
                {
                    args.add( name + "=" );
                }
                else
                {
                    String appender = "=";
                    for ( Object object : values )
                    {
                        args.add( name + appender + object.toString() );
                        appender = "+=";
                    }
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
