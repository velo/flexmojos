package api;

import org.sonatype.flexmojos.compiler.CompilerThreadLocal;

import flex2.compiler.common.LocalFilePathResolver;
import flex2.compiler.common.PathResolver;
import flex2.compiler.common.SinglePathResolver;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.compiler.util.URLPathResolver;

public class API2
{

    public static void usePathResolver( SinglePathResolver resolver )
    {
        PathResolver pathResolver = new PathResolver();
        if ( resolver != null )
        {
            pathResolver.addSinglePathResolver( resolver );
        }
        pathResolver.addSinglePathResolver( LocalFilePathResolver.getSingleton() );
        pathResolver.addSinglePathResolver( URLPathResolver.getSingleton() );
        ThreadLocalToolkit.setPathResolver( pathResolver );

        pathResolver.addSinglePathResolver( CompilerThreadLocal.pathResolver.get() );
    }
}
