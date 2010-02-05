package org.sonatype.flexmojos.compiler.util;

import flex2.compiler.Logger;
import flex2.compiler.common.PathResolver;
import flex2.compiler.common.SinglePathResolver;
import flex2.compiler.util.ConsoleLogger;

public class ThreadLocalToolkitHelper
{
    // only used to unit test this
    public static boolean invoked = false;

    private static Logger mavenLogger;

    private static SinglePathResolver mavenResolver;

    public static Logger fixLogger( Logger logger )
    {
        invoked = true;

        if ( logger instanceof ConsoleLogger )
        {
            if ( mavenLogger == null )
            {
                throw new IllegalStateException( "ThreadLocalToolkitHelper.mavenLogger was not initialized correctly" );
            }
            return mavenLogger;
        }

        return logger;
    }

    public static PathResolver fixPathResolver( PathResolver r )
    {
        invoked = true;

        if ( r != null )
        {
            if ( mavenResolver == null )
            {
                throw new IllegalStateException( "ThreadLocalToolkitHelper.mavenResolver was not initialized correctly" );
            }
            r.addSinglePathResolver( mavenResolver );
        }

        return r;
    }

    public static void setMavenLogger( Logger mavenLogger )
    {
        ThreadLocalToolkitHelper.mavenLogger = mavenLogger;
    }

    public static void setMavenResolver( SinglePathResolver mavenResolver )
    {
        ThreadLocalToolkitHelper.mavenResolver = mavenResolver;
    }

}
