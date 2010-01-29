/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
