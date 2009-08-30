package org.sonatype.flexmojos.generator.iface;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ws.jaxme.js.JavaQName;
import org.apache.ws.jaxme.js.JavaQNameImpl;
import org.apache.ws.jaxme.js.JavaSource;
import org.apache.ws.jaxme.js.JavaSourceFactory;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.flexmojos.generator.api.GenerationException;
import org.sonatype.flexmojos.generator.api.GenerationRequest;
import org.sonatype.flexmojos.generator.api.Generator;
import org.sonatype.flexmojos.generator.iface.model.Definition;
import org.sonatype.flexmojos.generator.iface.model.Excludes;
import org.sonatype.flexmojos.generator.iface.model.ForceArrays;
import org.sonatype.flexmojos.generator.iface.model.MethodSignature;

import com.thoughtworks.xstream.XStream;

@Component( role = Generator.class, hint = "internal-ifaces" )
public final class InternalIFacesGenerator
    extends AbstractLogEnabled
    implements Generator
{

    private final XStream xstream;

    public InternalIFacesGenerator()
    {
        this.xstream = new XStream();
        xstream.processAnnotations( Excludes.class );
        xstream.processAnnotations( ForceArrays.class );
        xstream.processAnnotations( Definition.class );
        xstream.processAnnotations( Method.class );
    }

    public void generate( GenerationRequest request )
        throws GenerationException
    {

        Map<Class<?>, Set<MethodSignature>> ignores = getIgnores( request );
        Map<Class<?>, Set<MethodSignature>> forceArrays = getForceArrays( request );

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

            JavaSourceFactory factory = new JavaSourceFactory();
            JavaQName className =
                JavaQNameImpl.getInstance( "org.sonatype.flexmojos.compiler", "I" + clazz.getSimpleName() );
            JavaSource js = factory.newJavaSource( className, "public" );
            js.setType( JavaSource.INTERFACE );

            Method[] methods = clazz.getDeclaredMethods();

            List<String> methodNames = new ArrayList<String>();
            Set<MethodSignature> signatures = ignores.get( clazz );

            for ( Method method : methods )
            {
                MethodSignature signature = new MethodSignature( method );
                if ( signatures.contains( signature ) )
                {
                    signatures.remove( signature );
                    continue;
                }

                if ( Modifier.isPrivate( method.getModifiers() ) )
                {
                    continue;
                }

                String name = method.getName();
                boolean isAdd = false;
                if ( name.startsWith( "add" ) )
                {
                    isAdd = true;
                }

                name = StringUtil.removePrefix( name );
                name = StringUtils.capitalizeFirstLetter( name );

                Class<?>[] args = method.getParameterTypes();

                if ( args.length == 0 )
                {
                    // makes no sense to generate a void get method
                    continue;
                }

                if ( method.getName().startsWith( "set" ) && hasAddMethod( clazz, name, args ) )
                {
                    continue;
                }

                String methodName = getMethodName( methodNames, name, args );

                String argType;

                if ( args.length == 1 )
                {
                    argType = args[0].getSimpleName();
                    if ( !args[0].isPrimitive() && !args[0].isArray() )
                        js.addImport( args[0] );
                    if ( args[0].isArray() )
                        js.addImport( args[0].getComponentType() );
                }
                else
                {
                    JavaQName argName = JavaQNameImpl.getInstance( "org.sonatype.flexmojos.compiler", "I" + name );
                    if ( factory.getJavaSource( argName ) != null )
                    {
                        continue;
                    }

                    JavaSource argClass = factory.newJavaSource( argName, "public" );
                    argClass.setType( JavaSource.INTERFACE );

                    for ( int i = 0; i < args.length; i++ )
                    {
                        argClass.newJavaMethod( "get" + name + "Arg" + i, args[i] );
                    }

                    argType = argName.getClassName();
                }

                boolean isArray = isAdd && !args[0].isArray();
                if ( !isArray )
                {
                    isArray = forceArrays.get( clazz ).contains( signature );
                }

                if ( isArray )
                {
                    js.newJavaMethod( methodName, argType + "[]" );
                }
                else
                {
                    js.newJavaMethod( methodName, argType );
                }

            }

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

    private boolean hasAddMethod( Class<?> clazz, String name, Class<?>[] args )
    {
        try
        {
            clazz.getMethod( "add" + name, args );
            return true;
        }
        catch ( NoSuchMethodException e )
        {
            try
            {
                clazz.getMethod( "add" + name, simplify( args ) );
                return true;
            }
            catch ( NoSuchMethodException e2 )
            {
                // ignore
            }
        }

        return false;
    }

    private String getMethodName( List<String> methodNames, String name, Class<?>[] args )
    {
        String prefix;

        if ( args.length == 1 && ( args[0].equals( Boolean.class ) || args[0].equals( boolean.class ) ) )
        {
            prefix = "is";
        }
        else
        {
            prefix = "get";
        }

        String methodName = prefix + name;

        if ( methodNames.contains( methodName ) )
        {
            int i = 1;
            while ( methodNames.contains( methodName ) )
            {
                methodName = prefix + i + name;
            }
        }
        else
        {
            methodNames.add( methodName );
        }
        return methodName;
    }

    private Map<Class<?>, Set<MethodSignature>> getIgnores( GenerationRequest request )
    {
        if ( !request.getExtraOptions().containsKey( Excludes.NAME ) )
        {
            return Collections.emptyMap();
        }

        Excludes ignoresL = (Excludes) this.xstream.fromXML( request.getExtraOptions().get( Excludes.NAME ) );
        List<Definition> ignores = ignoresL.getExcludes();

        Map<Class<?>, Set<MethodSignature>> map = new LinkedHashMap<Class<?>, Set<MethodSignature>>();
        for ( Definition ignore : ignores )
        {
            map.put( ignore.getClassname(), ignore.getMethods() );
        }
        return map;
    }

    private Map<Class<?>, Set<MethodSignature>> getForceArrays( GenerationRequest request )
    {
        if ( !request.getExtraOptions().containsKey( ForceArrays.NAME ) )
        {
            return Collections.emptyMap();
        }

        ForceArrays arrays = (ForceArrays) this.xstream.fromXML( request.getExtraOptions().get( ForceArrays.NAME ) );
        List<Definition> ignores = arrays.getSignatures();

        Map<Class<?>, Set<MethodSignature>> map = new LinkedHashMap<Class<?>, Set<MethodSignature>>();
        for ( Definition ignore : ignores )
        {
            map.put( ignore.getClassname(), ignore.getMethods() );
        }
        return map;
    }

    private Class<?>[] simplify( Class<?>[] args )
    {
        Class<?>[] simpleArgs = new Class<?>[args.length];
        for ( int i = 0; i < args.length; i++ )
        {
            Class<?> clazz = args[i];
            if ( !clazz.isArray() )
            {
                simpleArgs[i] = clazz;
            }
            else
            {
                simpleArgs[i] = clazz.getComponentType();
            }
        }
        return simpleArgs;
    }

}
