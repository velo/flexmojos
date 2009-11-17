import org.objectweb.asm.util.ASMifierClassVisitor;

public class DevMain
{
    public static void main( String[] args )
        throws Exception
    {
        ASMifierClassVisitor.main( new String[] { API2.class.getName() } );
    }

}
