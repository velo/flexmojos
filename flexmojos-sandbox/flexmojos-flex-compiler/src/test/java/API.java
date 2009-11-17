import flex2.compiler.common.LocalFilePathResolver;
import flex2.compiler.common.PathResolver;
import flex2.compiler.common.SinglePathResolver;
import flex2.compiler.util.ConsoleLogger;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.compiler.util.URLPathResolver;

public class API
{
    public static void useConsoleLogger( boolean isInfoEnabled, boolean isDebugEnabled, boolean isWarningEnabled,
                                         boolean isErrorEnabled )
    {
        ThreadLocalToolkit.setLogger( new ConsoleLogger( isInfoEnabled, isDebugEnabled, isWarningEnabled,
                                                         isErrorEnabled ) );
    }

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
    }
}
