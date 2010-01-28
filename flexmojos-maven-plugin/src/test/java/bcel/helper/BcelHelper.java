package bcel.helper;

import java.io.IOException;
import java.io.InputStream;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.BCELifier;

public class BcelHelper
{
    public static void main( String[] args ) throws Error, Exception
    {
        print( Original.class );
        print( Tampered.class );
    }

    private static void print( Class<?> clazz )
        throws IOException, ClassFormatError
    {
        System.out.println("\n\n ===== Bcelifiering: " + clazz);
        String classname = clazz.getName();
        String classFile = "/" + classname.replace( '.', '/' ) + ".class";
        InputStream source = BcelHelper.class.getResourceAsStream( classFile );

        ClassParser cp = new ClassParser( source, "ajota" );
        JavaClass jc = cp.parse();

        BCELifier b = new BCELifier( jc, System.out );
        b.start();
    }
}
