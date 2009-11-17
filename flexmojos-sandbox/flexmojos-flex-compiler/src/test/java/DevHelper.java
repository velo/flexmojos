import org.objectweb.asm.util.ASMifierClassVisitor;

public class DevHelper
{
    public static void main( String[] args )
        throws Exception
    {
        System.out.println( "================== original" );
        ASMifierClassVisitor.main( new String[] { API.class.getName() } );

        System.out.println( "================== improved" );
        ASMifierClassVisitor.main( new String[] { API2.class.getName() } );
    }

}
