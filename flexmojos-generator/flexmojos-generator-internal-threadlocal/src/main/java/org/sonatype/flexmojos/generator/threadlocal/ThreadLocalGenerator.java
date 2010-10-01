package org.sonatype.flexmojos.generator.threadlocal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.flexmojos.generator.GenerationException;
import org.sonatype.flexmojos.generator.GenerationRequest;
import org.sonatype.flexmojos.generator.Generator;

@Component( role = Generator.class, hint = "thread-local" )
public class ThreadLocalGenerator
    extends AbstractLogEnabled
    implements Generator
{

    private static final String FLEXMOJOS_HELPER = "org.sonatype.flexmojos.compiler.util.ThreadLocalToolkitHelper";

    private static final String THREAD_LOCAL_TOOLKIT = "flex2.compiler.util.ThreadLocalToolkit";

    public void generate( GenerationRequest request )
        throws GenerationException
    {
        Map<String, File> classes = request.getClasses();
        if ( !classes.containsKey( THREAD_LOCAL_TOOLKIT ) )
        {
            throw new GenerationException( "Class not found: " + THREAD_LOCAL_TOOLKIT );
        }

        try
        {
            instrumentThreadLocalToolkit( classes.get( THREAD_LOCAL_TOOLKIT ), request.getTransientOutputFolder() );
        }
        catch ( IOException e )
        {
            throw new GenerationException( "Failed to generated wrapper " + THREAD_LOCAL_TOOLKIT, e );
        }
    }

    private static void instrumentThreadLocalToolkit( File mxmlcJar, File destination )
        throws IOException
    {
        String classfile = THREAD_LOCAL_TOOLKIT.replace( '.', '/' ) + ".class";

        JavaClass jc;
        ZipFile jar = null;
        InputStream source = null;
        try
        {
            jar = new ZipFile( mxmlcJar );

            ZipEntry entry = jar.getEntry( classfile );
            source = jar.getInputStream( entry );

            ClassParser p = new ClassParser( source, THREAD_LOCAL_TOOLKIT );
            jc = p.parse();
        }
        finally
        {
            IOUtil.close( source );
            if ( jar != null )
            {
                jar.close();
            }
        }

        ClassGen cg = new ClassGen( jc );
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory _factory = new InstructionFactory( cg, cp );

        Method[] methods = cg.getMethods();
        for ( Method method : methods )
        {
            Type[] types = method.getArgumentTypes();
            if ( method.getName().equals( "setLogger" ) && types.length == 1
                && types[0].equals( Type.getType( "Lflex2/compiler/Logger;" ) ) )
            {
                InstructionList il = new InstructionList();
                MethodGen mg =
                    new MethodGen( method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null,
                                   method.getName(), null, il, cp );

                il.append( InstructionFactory.createLoad( Type.OBJECT, 0 ) );
                il.append( _factory.createInvoke( FLEXMOJOS_HELPER, "fixLogger",
                                                  new ObjectType( "flex2.compiler.Logger" ),
                                                  new Type[] { new ObjectType( "flex2.compiler.Logger" ) },
                                                  Constants.INVOKESTATIC ) );
                il.append( InstructionFactory.createStore( Type.OBJECT, 0 ) );

                il.append( _factory.createFieldAccess( THREAD_LOCAL_TOOLKIT, "logger",
                                                       new ObjectType( "java.lang.ThreadLocal" ), Constants.GETSTATIC ) );
                il.append( InstructionFactory.createLoad( Type.OBJECT, 0 ) );
                il.append( _factory.createInvoke( "java.lang.ThreadLocal", "set", Type.VOID,
                                                  new Type[] { Type.OBJECT }, Constants.INVOKEVIRTUAL ) );
                il.append( InstructionFactory.createLoad( Type.OBJECT, 0 ) );
                BranchInstruction ifnull_13 = InstructionFactory.createBranchInstruction( Constants.IFNULL, null );
                il.append( ifnull_13 );
                il.append( InstructionFactory.createLoad( Type.OBJECT, 0 ) );
                il.append( _factory.createInvoke( THREAD_LOCAL_TOOLKIT, "getLocalizationManager",
                                                  new ObjectType( "flash.localization.LocalizationManager" ),
                                                  Type.NO_ARGS, Constants.INVOKESTATIC ) );
                il.append( _factory.createInvoke( "flex2.compiler.Logger",
                                                  "setLocalizationManager",
                                                  Type.VOID,
                                                  new Type[] { new ObjectType( "flash.localization.LocalizationManager" ) },
                                                  Constants.INVOKEINTERFACE ) );
                InstructionHandle ih_25 = il.append( InstructionFactory.createReturn( Type.VOID ) );
                ifnull_13.setTarget( ih_25 );
                mg.setMaxStack();
                mg.setMaxLocals();

                cg.removeMethod( method );
                cg.addMethod( mg.getMethod() );

                il.dispose();
            }
            else if ( method.getName().equals( "setPathResolver" ) && types.length == 1
                && types[0].equals( new ObjectType( "flex2.compiler.common.PathResolver" ) ) )
            {
                InstructionList il = new InstructionList();
                MethodGen mg =
                    new MethodGen( method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null,
                                   method.getName(), null, il, cp );

                il.append( InstructionFactory.createLoad( Type.OBJECT, 0 ) );
                il.append( _factory.createInvoke( FLEXMOJOS_HELPER,
                                                  "fixPathResolver",
                                                  new ObjectType( "flex2.compiler.common.PathResolver" ),
                                                  new Type[] { new ObjectType( "flex2.compiler.common.PathResolver" ) },
                                                  Constants.INVOKESTATIC ) );
                il.append( InstructionFactory.createStore( Type.OBJECT, 0 ) );

                il.append( _factory.createFieldAccess( THREAD_LOCAL_TOOLKIT, "resolver",
                                                       new ObjectType( "java.lang.ThreadLocal" ), Constants.GETSTATIC ) );
                il.append( InstructionFactory.createLoad( Type.OBJECT, 0 ) );
                il.append( _factory.createInvoke( "java.lang.ThreadLocal", "set", Type.VOID,
                                                  new Type[] { Type.OBJECT }, Constants.INVOKEVIRTUAL ) );
                il.append( InstructionFactory.createReturn( Type.VOID ) );

                mg.setMaxStack();
                mg.setMaxLocals();

                cg.removeMethod( method );
                cg.addMethod( mg.getMethod() );

                il.dispose();
            }
        }

        FieldGen assertor = new FieldGen( Constants.ACC_PUBLIC & Constants.ACC_STATIC, Type.STRING, "assertor", cp );
        cg.addField( assertor.getField() );

        FileOutputStream output = null;
        try
        {
            File destFile = new File( destination, classfile );
            destFile.getParentFile().mkdirs();
            output = new FileOutputStream( destFile );
            cg.getJavaClass().dump( output );
            output.flush();
        }
        finally
        {
            IOUtil.close( output );
        }
    }

}
