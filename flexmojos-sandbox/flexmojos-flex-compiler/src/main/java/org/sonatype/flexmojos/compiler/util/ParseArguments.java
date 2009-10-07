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
