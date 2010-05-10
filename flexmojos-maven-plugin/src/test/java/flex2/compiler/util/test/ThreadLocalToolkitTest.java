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
package flex2.compiler.util.test;

import static org.mockito.Mockito.mock;

import org.sonatype.flexmojos.compiler.util.ThreadLocalToolkitHelper;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import flex2.compiler.Logger;
import flex2.compiler.common.PathResolver;
import flex2.compiler.util.ConsoleLogger;
import flex2.compiler.util.ThreadLocalToolkit;

public class ThreadLocalToolkitTest
{

    @BeforeMethod
    public void reset()
    {
        ThreadLocalToolkitHelper.invoked = false;
    }

    @Test
    public void setLogger()
        throws Exception
    {
        ThreadLocalToolkitHelper.setMavenLogger( mock( Logger.class ) );
        ThreadLocalToolkit.setLogger( new ConsoleLogger() );
        Assert.assertTrue( ThreadLocalToolkitHelper.invoked );
    }

    @Test
    public void setPathResolver()
        throws Exception
    {
        ThreadLocalToolkitHelper.setMavenResolver( mock( PathResolver.class ) );
        ThreadLocalToolkit.setPathResolver( mock( PathResolver.class ) );
        Assert.assertTrue( ThreadLocalToolkitHelper.invoked );
    }

}
