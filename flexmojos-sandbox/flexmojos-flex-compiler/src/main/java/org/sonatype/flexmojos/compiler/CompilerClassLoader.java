package org.sonatype.flexmojos.compiler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.sonatype.flexmojos.compiler.visitors.APIClassVisitor;

public class CompilerClassLoader
    extends ClassLoader
{

    public CompilerClassLoader( ClassLoader cl )
    {
        super( cl );
    }

    public Class<?> loadAPI()
        throws ClassNotFoundException
    {
        final String className = "flex2.compiler.API";
        try
        {
            ClassWriter cw = new ClassWriter( true, true );
            ClassVisitor ncv = new APIClassVisitor( cw );

            ClassReader cr = new ClassReader( className );
            cr.accept( ncv, false );
            byte[] bytecode = cw.toByteArray();

            return super.defineClass( className, bytecode, 0, bytecode.length );
        }
        catch ( Exception ex )
        {
            throw new ClassNotFoundException( "Load error: " + ex.toString(), ex );
        }
    }

}
