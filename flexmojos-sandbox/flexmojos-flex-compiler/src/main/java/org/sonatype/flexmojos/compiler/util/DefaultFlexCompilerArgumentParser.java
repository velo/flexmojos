package org.sonatype.flexmojos.compiler.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.flexmojos.compiler.IASDocConfiguration;
import org.sonatype.flexmojos.compiler.IFlexArgument;
import org.sonatype.flexmojos.compiler.IFlexConfiguration;
import org.sonatype.flexmojos.compiler.IFontsConfiguration;
import org.sonatype.flexmojos.compiler.IMetadataConfiguration;
import org.sonatype.flexmojos.compiler.IRuntimeSharedLibraryPath;
import org.sonatype.flexmojos.generator.iface.StringUtil;

@Component( role = FlexCompilerArgumentParser.class )
public class DefaultFlexCompilerArgumentParser
    extends AbstractLogEnabled
    implements FlexCompilerArgumentParser
{

    public <E> String[] parseArguments( E cfg, Class<? extends E> configClass )
    {
        return parseArguments( cfg, configClass, Thread.currentThread().getContextClassLoader() );
    }

    public <E> String[] parseArguments( E cfg, Class<? extends E> configClass, ClassLoader classLoader )
    {
        String[] args = getArgumentsList( cfg, configClass, classLoader ).toArray( new String[0] );
        return args;
    }

    public <E> List<String> getArgumentsList( E cfg, Class<? extends E> configClass )
    {
        return getArgumentsList( cfg, configClass, Thread.currentThread().getContextClassLoader() );
    }

    public <E> List<String> getArgumentsList( E cfg, Class<? extends E> configClass, ClassLoader classLoader )
    {
        List<Entry<String, List<String>>> charArgs;
        try
        {
            charArgs = doGetArgs( cfg, configClass, classLoader );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }

        List<String> args = new ArrayList<String>();
        for ( Entry<String, List<String>> arg : charArgs )
        {
            args.add( "-" + arg.getName() );
            if ( arg.getValue() != null )
            {
                args.addAll( arg.getValue() );
            }
        }
        return args;
    }

    @SuppressWarnings( "unchecked" )
    private <E> List<Entry<String, List<String>>> doGetArgs( E cfg, Class<? extends E> configClass,
                                                             ClassLoader classLoader )
        throws Exception
    {
        if ( cfg == null )
        {
            return Collections.emptyList();
        }

        configClass = (Class<? extends E>) classLoader.loadClass( configClass.getName() );

        List<Entry<String, List<String>>> args = new LinkedList<Entry<String, List<String>>>();

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

            String name = parseName( method.getName() );

            if ( value instanceof IFlexConfiguration )
            {
                List<Entry<String, List<String>>> subArgs = doGetArgs( value, returnType, classLoader );
                String configurationName = parseConfigurationName( method.getName() );
                for ( Entry<String, List<String>> arg : subArgs )
                {
                    args.add( new Entry<String, List<String>>( configurationName + "." + arg.getName(), arg.getValue() ) );
                }
            }
            else if ( cfg instanceof IASDocConfiguration && "footer".equals( name ) )
            {
                args.add( new Entry<String, List<String>>( name, Collections.singletonList( value.toString() ) ) );
            }
            else if ( cfg instanceof IMetadataConfiguration && "description".equals( name ) )
            {
                args.add( new Entry<String, List<String>>( name, Collections.singletonList( value.toString() ) ) );
            }
            else if ( cfg instanceof IFontsConfiguration && "managers".equals( name ) )
            {
                args.add( new Entry<String, List<String>>( name, (List<String>) value ) );
            }
            else if ( value instanceof IRuntimeSharedLibraryPath || value instanceof IRuntimeSharedLibraryPath[] )
            {
                IRuntimeSharedLibraryPath[] values;
                Class<?> type = returnType;
                if ( type.isArray() )
                {
                    values = (IRuntimeSharedLibraryPath[]) value;
                }
                else
                {
                    values = new IRuntimeSharedLibraryPath[] { (IRuntimeSharedLibraryPath) value };
                }

                for ( IRuntimeSharedLibraryPath arg : values )
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append( arg.pathElement() );

                    Set<java.util.Map.Entry<String, String>> urls = arg.rslUrl().entrySet();
                    for ( java.util.Map.Entry<String, String> entry : urls )
                    {
                        sb.append( ',' );
                        sb.append( entry.getKey() );

                        if ( entry.getValue() != null )
                        {
                            sb.append( ',' );
                            sb.append( entry.getValue() );
                        }
                    }

                    args.add( new Entry<String, List<String>>( name + "=" + sb, null ) );
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
                    List<String> subArg = new LinkedList<String>();
                    for ( String argMethodName : order )
                    {
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
                                subArg.add( iterator.next().toString() );
                            }
                        }
                        else if ( argValue instanceof Map<?, ?> )
                        {
                            Map<?, ?> map = ( (Map<?, ?>) argValue );
                            Set<?> argValues = map.entrySet();
                            for ( Iterator<?> iterator = argValues.iterator(); iterator.hasNext(); )
                            {
                                java.util.Map.Entry<?, ?> entry = (java.util.Map.Entry<?, ?>) iterator.next();
                                subArg.add( entry.getKey().toString() );
                                if ( entry.getValue() != null )
                                {
                                    subArg.add( entry.getValue().toString() );
                                }
                            }

                        }
                        else
                        {
                            subArg.add( argValue.toString() );
                        }
                    }

                    args.add( new Entry<String, List<String>>( name, subArg ) );
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
                if ( values.length == 0 )
                {
                    args.add( new Entry<String, List<String>>( name + "=", null ) );
                }
                else if ( "include-classes".equals( name ) )
                {
                    StringBuilder classes = new StringBuilder();
                    for ( Object vl : values )
                    {
                        if ( classes.length() != 0 )
                        {
                            classes.append( "," );
                        }
                        classes.append( vl );
                    }
                    args.add( new Entry<String, List<String>>( name + "=" + classes, null ) );
                }
                else
                {
                    String appender = "=";
                    for ( Object object : values )
                    {
                        args.add( new Entry<String, List<String>>( name + appender + object.toString(), null ) );
                        appender = "+=";
                    }
                }
            }
            else
            {
                args.add( new Entry<String, List<String>>( name + "=" + value.toString(), null ) );
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
