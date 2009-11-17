package org.sonatype.flexmojos.compiler;

import java.io.InputStream;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.sonatype.flexmojos.compiler.visitors.APIClassVisitor;

public class CompilerClassLoader
    extends ClassLoader
{

    private static final String compilerApi = "flex2.compiler.CompilerAPI";

    public CompilerClassLoader( ClassLoader cl )
    {
        super( cl );
    }

    public Class<?> loadAPI()
        throws Exception
    {
        ZipFile mxmlc =
            new ZipFile(
                         "C:/Users/Seven/.m2/repository/com/adobe/flex/compiler/mxmlc/4.0.0.10485/mxmlc-4.0.0.10485.jar" );
        InputStream in = mxmlc.getInputStream( mxmlc.getEntry( "flex2/compiler/CompilerAPI.class" ) );

        try
        {
            ClassReader cr = new ClassReader( in );
            ClassWriter cw = new ClassWriter( cr, 0 );
            ClassVisitor cv = new APIClassVisitor( cw );

            cr.accept( cv, ClassReader.EXPAND_FRAMES );
            byte[] bytecode = cw.toByteArray();

            return super.defineClass( compilerApi, bytecode, 0, bytecode.length );
        }
        finally
        {
            in.close();
            mxmlc.close();
        }
    }

}
