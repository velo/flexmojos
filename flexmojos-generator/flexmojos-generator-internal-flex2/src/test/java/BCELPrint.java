import java.io.InputStream;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.BCELifier;

import flex2.tools.Compc;

public class BCELPrint
{
    public static void main( String[] args )
        throws Exception
    {
        Class<?> clazz = Compc.class;
        String classname = clazz.getName();
        String classFile = "/" + classname.replace( '.', '/' ) + ".class";
        InputStream in = BCELPrint.class.getResourceAsStream( classFile );

        ClassParser p = new ClassParser( in, "flex2.tool.Compc" );
        JavaClass jc = p.parse();

        BCELifier b = new BCELifier( jc, System.out );
        b.start();
    }
}
