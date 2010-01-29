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
            return mavenLogger;
        }

        System.out.println( getClass( logger ) );

        return logger;
    }

    public static PathResolver fixPathResolver( PathResolver r )
    {
        invoked = true;

        System.out.println( getClass( r ) );

        if ( r != null )
        {
            r.addSinglePathResolver( mavenResolver );
        }

        return r;
    }

    private static String getClass( Object o )
    {
        if ( o != null )
        {
            return o.getClass().getName();
        }
        return null;
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
