package org.sonatype.flexmojos.generator.iface;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.ws.jaxme.js.JavaQName;
import org.apache.ws.jaxme.js.JavaQNameImpl;
import org.apache.ws.jaxme.js.JavaSource;
import org.apache.ws.jaxme.js.JavaSourceFactory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.flexmojos.generator.GenerationException;
import org.sonatype.flexmojos.generator.GenerationRequest;
import org.sonatype.flexmojos.generator.Generator;

import flex2.compiler.config.ConfigurationInfo;
import flex2.compiler.config.ConfigurationValue;

@Component( role = Generator.class, hint = "internal-ifaces" )
public final class InternalIFacesGenerator
    extends AbstractLogEnabled
    implements Generator
{

    private static final String PACKAGE = "org.sonatype.flexmojos.compiler";

    private static final String CFG_PREFIX = "cfg";

    private static final String GET_PREFIX = "get";

    private static final String CONFIGURATION_SUFFIX = "Configuration";

    private static final String INFO_SUFFIX = "Info";

    public InternalIFacesGenerator()
    {
    }

    public void generate( GenerationRequest request )
        throws GenerationException
    {
        JavaSourceFactory factory = new JavaSourceFactory();

        JavaQName ann = JavaQNameImpl.getInstance( PACKAGE, "IFlexConfiguration" );
        JavaSource annSource = factory.newJavaSource( ann, "public" );
        annSource.setType( JavaSource.INTERFACE );

        JavaQName arg = JavaQNameImpl.getInstance( PACKAGE, "IFlexArgument" );
        JavaSource argSource = factory.newJavaSource( arg, "public" );
        argSource.setType( JavaSource.INTERFACE );

        for ( String classname : request.getClasses().keySet() )
        {
            Class<?> clazz;
            try
            {
                clazz = request.getClassLoader().loadClass( classname );
            }
            catch ( ClassNotFoundException e )
            {
                throw new GenerationException( e.getMessage(), e );
            }

            getMethods( clazz, factory, ann, arg );

            File outDir = request.getTransientOutputFolder();
            outDir.mkdirs();

            try
            {
                factory.write( outDir );
            }
            catch ( Exception e )
            {
                throw new GenerationException( "Error generating " + clazz.getName(), e );
            }
        }
    }

    private String varname( String membername, String basename )
    {
        return ( ( basename == null ) ? membername : ( basename + "." + membername ) );
    }

    private JavaQName getMethods( Class<?> clazz, JavaSourceFactory factory, JavaQName ann, JavaQName arg )
        throws GenerationException
    {
        JavaQName className = JavaQNameImpl.getInstance( PACKAGE, "I" + clazz.getSimpleName() );
        if ( factory.getJavaSource( className ) != null )
        {
            return className;
        }

        JavaSource js = factory.newJavaSource( className, "public" );
        js.setType( JavaSource.INTERFACE );
        js.addExtends( ann );

        Method methods[] = clazz.getMethods();

        for ( int m = 0; m < methods.length; ++m )
        {
            Method method = methods[m];

            if ( method.getName().startsWith( CFG_PREFIX ) )
            {
                Class<?>[] pt = method.getParameterTypes();

                if ( ( pt.length > 1 )
                    && ( pt[0].getCanonicalName().equals( ConfigurationValue.class.getCanonicalName() ) ) )
                {
                    // This is an autoconfiguration setter!

                    ConfigurationInfo info = createInfo( method );

                    String leafname = method.getName().substring( CFG_PREFIX.length() );
                    String name = varname( leafname, null );

                    JavaQName type;
                    int args = info.getArgCount();
                    if ( args == 0 )
                    {
                        continue;
                    }
                    else if ( "RuntimeSharedLibraryPath".equals( name ) )
                    {
                        type = generateSubclass( factory, arg, info, name, 2, String.class, Map.class );
                    }
                    else if ( "Extension".equals( name ) )
                    {
                        type = generateSubclass( factory, arg, info, name, 2, File.class, String[].class );
                    }
                    else if ( "Frame".equals( name ) )
                    {
                        type = generateSubclass( factory, arg, info, name, 2, String.class, String[].class );
                    }
                    else if ( args == 1 )
                    {
                        type = JavaQNameImpl.getInstance( getArgType( info, 0 ) );
                    }
                    else if ( args < 0 )
                    {
                        Object argnames;
                        try
                        {
                            Field f = ConfigurationInfo.class.getDeclaredField( "argnames" );
                            f.setAccessible( true );
                            argnames = f.get( info );
                        }
                        catch ( Exception e )
                        {
                            throw new GenerationException( e.getMessage(), e );
                        }
                        if ( argnames != null && argnames.getClass().isArray() && ( (Object[]) argnames ).length != 1 )
                        {
                            type = generateSubclass( factory, arg, info, name, ( (Object[]) argnames ).length );
                        }
                        else
                        {
                            type = JavaQNameImpl.getInstance( method.getParameterTypes()[1] );
                        }
                    }
                    else
                    {
                        type = generateSubclass( factory, arg, info, name, args );
                    }

                    if ( info.isPath() && args <= 1 )
                    {
                        type = JavaQNameImpl.getInstance( File.class );
                    }

                    type = promoteWrappers( type );

                    if ( info.allowMultiple()
                        && !( type.isArray() || type.equals( JavaQNameImpl.getInstance( List.class ) ) ) )
                    {
                        type = JavaQNameImpl.getArray( type );
                    }

                    /* JavaMethod nm = */js.newJavaMethod( GET_PREFIX + name, type );

                    StringBuilder order = new StringBuilder();
                    order.append( "  String " + StringUtil.toUpperCamelCase( name ) + " = " );
                    order.append( '"' ).append( GET_PREFIX ).append( name ).append( '"' ).append( ';' );

                    js.addRawJavaSource( order.toString() );
                }
            }
        }

        // Now find all children.
        for ( int m = 0; m < methods.length; ++m )
        {
            Method method = methods[m];

            String name = method.getName();
            if ( name.startsWith( GET_PREFIX ) && name.endsWith( CONFIGURATION_SUFFIX ) )
            {
                JavaQName source = getMethods( method.getReturnType(), factory, ann, arg );
                js.newJavaMethod( name, source );
            }
            else
            {
                continue;
            }
        }

        return className;
    }

    private JavaQName generateSubclass( JavaSourceFactory factory, JavaQName arg, ConfigurationInfo info, String name,
                                        int args, Class<?>... typeClasses )
    {
        JavaQName type;
        type = JavaQNameImpl.getInstance( PACKAGE, "I" + name );

        if ( factory.getJavaSource( type ) == null )
        {
            JavaSource subClass = factory.newJavaSource( type, "public" );
            subClass.setType( JavaSource.INTERFACE );
            subClass.addExtends( arg );

            StringBuilder order = new StringBuilder();
            order.append( "  String[] ORDER = new String[] {" );
            for ( int i = 0; i < args; i++ )
            {
                Class<?> argType = getArgType( info, i, typeClasses );
                String argName = info.getArgName( i );
                argName = StringUtil.toCamelCase( argName );
                subClass.newJavaMethod( argName, argType );

                order.append( '"' ).append( argName ).append( '"' ).append( ", " );
            }
            order.append( " };" );

            subClass.addRawJavaSource( order.toString() );
        }
        return type;
    }

    private JavaQName promoteWrappers( JavaQName type )
        throws GenerationException
    {
        if ( !type.isPrimitive() )
        {
            return type;
        }

        if ( JavaQNameImpl.BOOLEAN.equals( type ) )
        {
            return JavaQNameImpl.getInstance( Boolean.class );
        }
        else if ( JavaQNameImpl.BYTE.equals( type ) )
        {
            return JavaQNameImpl.getInstance( Byte.class );
        }
        else if ( JavaQNameImpl.CHAR.equals( type ) )
        {
            return JavaQNameImpl.getInstance( Character.class );
        }
        else if ( JavaQNameImpl.DOUBLE.equals( type ) )
        {
            return JavaQNameImpl.getInstance( Double.class );
        }
        else if ( JavaQNameImpl.FLOAT.equals( type ) )
        {
            return JavaQNameImpl.getInstance( Float.class );
        }
        else if ( JavaQNameImpl.INT.equals( type ) )
        {
            return JavaQNameImpl.getInstance( Integer.class );
        }
        else if ( JavaQNameImpl.LONG.equals( type ) )
        {
            return JavaQNameImpl.getInstance( Long.class );
        }
        else if ( JavaQNameImpl.SHORT.equals( type ) )
        {
            return JavaQNameImpl.getInstance( Short.class );
        }
        else
        {
            throw new GenerationException( "Invalid primitive type: " + type );
        }
    }

    private Class<?> getArgType( ConfigurationInfo info, int i, Class<?>... typeClasses )
    {
        if ( typeClasses != null && i <= typeClasses.length - 1 )
        {
            return typeClasses[i];
        }

        Class<?> argType;
        try
        {
            argType = info.getArgType( i );
        }
        catch ( NullPointerException e )
        {
            argType = String.class;
        }
        return argType;
    }

    private static ConfigurationInfo createInfo( Method setterMethod )
        throws GenerationException
    {
        ConfigurationInfo info = null;

        String infoMethodName = GET_PREFIX + setterMethod.getName().substring( CFG_PREFIX.length() ) + INFO_SUFFIX;
        Class<?> cfgClass = setterMethod.getDeclaringClass();

        try
        {
            Method infoMethod = cfgClass.getMethod( infoMethodName );

            if ( !Modifier.isStatic( infoMethod.getModifiers() ) )
            {
                assert false : ( "coding error: " + cfgClass.getName() + "." + infoMethodName + " needs to be static!" );
                infoMethod = null;
            }

            info = (ConfigurationInfo) infoMethod.invoke( null );

        }
        catch ( NoSuchMethodException e )
        {
            // use default configuration
            info = new ConfigurationInfo();
        }
        catch ( Exception e )
        {
            System.out.println( Arrays.toString( cfgClass.getMethods() ) );
            throw new GenerationException( e.getMessage(), e );
        }

        // info.setSetterMethod( setterMethod );
        // info.setGetterMethod( getterMethod );

        return info;
    }

}
