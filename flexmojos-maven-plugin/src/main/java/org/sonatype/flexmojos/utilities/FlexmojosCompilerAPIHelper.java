package org.sonatype.flexmojos.utilities;

import flex2.compiler.Logger;
import flex2.compiler.common.PathResolver;

public class FlexmojosCompilerAPIHelper
{
    
    public static boolean invoked = false;

    public static Logger fixLogger( Logger logger )
    {
        invoked = true;
        // TODO
        return logger;
    }

    public static PathResolver fixPathResolver( PathResolver r )
    {
        invoked = true;
        // TODO
        return r;
    }

}
