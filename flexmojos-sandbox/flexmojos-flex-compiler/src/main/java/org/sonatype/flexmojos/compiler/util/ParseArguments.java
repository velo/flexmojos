package org.sonatype.flexmojos.compiler.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.sonatype.flexmojos.compiler.IFlexArgument;
import org.sonatype.flexmojos.compiler.IFlexConfiguration;
import org.sonatype.flexmojos.generator.iface.StringUtil;

public class ParseArguments
{

    public static <E> String[] getArguments( E cfg, Class<? extends E> configClass )
    {
        return getArgumentsList( cfg, configClass ).toArray( new String[0] );
    }

    public static <E> List<String> getArgumentsList( E cfg, Class<? extends E> configClass )
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
                        if ( argValue == null )
                        {
                            continue;
                        }
                        else if ( argValue instanceof Collection<?> || argValue.getClass().isArray() )
                        {
                            Collection<?> argValues;
                            if ( argValue.getClass().isArray() )
                            {
                                argValues = Arrays.asList( (Object[]) argValue );
                            }
                            else
                            {
                                argValues = (Collection<?>) argValue;
                            }
                            for ( Iterator<?> iterator = argValues.iterator(); iterator.hasNext(); )
                            {
                                arg.append( iterator.next().toString() );
                                if ( iterator.hasNext() )
                                {
                                    arg.append( ' ' );
                                }
                            }
                        }
                        else if ( argValue instanceof Map<?, ?> )
                        {
                            Map<?, ?> map = ( (Map<?, ?>) argValue );
                            Set<?> argValues = map.entrySet();
                            for ( Iterator<?> iterator = argValues.iterator(); iterator.hasNext(); )
                            {
                                Entry<?, ?> entry = (Entry<?, ?>) iterator.next();
                                arg.append( entry.getKey().toString() );
                                if ( entry.getValue() != null )
                                {
                                    arg.append( ' ' );
                                    arg.append( entry.getValue().toString() );
                                }
                                if ( iterator.hasNext() )
                                {
                                    arg.append( ' ' );
                                }
                            }

                        }
                        else
                        {
                            arg.append( argValue.toString() );
                        }
                    }

                    args.add( parseName( method.getName() ) + " " + arg );
                }
            }
            else if ( returnType.isArray() || value instanceof Collection<?> )
            {
                Object[] values;
                if ( returnType.isArray() )
                {
                    values = (Object[]) value;
                }
                else
                {
                    values = ( (Collection<?>) value ).toArray();
                }
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
