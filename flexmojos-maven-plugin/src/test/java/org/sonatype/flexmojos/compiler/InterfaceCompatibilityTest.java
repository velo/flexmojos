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
package org.sonatype.flexmojos.compiler;

import java.lang.reflect.Method;

import org.testng.Assert;
import org.testng.annotations.Test;

public class InterfaceCompatibilityTest
{

    @Test
    public void asdoc()
        throws Exception
    {
        checkInterfaceCompatibility( AsdocMojo.class, IASDocConfiguration.class, ICommandLineConfiguration.class,
                                     ICompcConfiguration.class );
    }

    @Test
    public void mxmlc()
        throws Exception
    {
        checkInterfaceCompatibility( MxmlcMojo.class, ICommandLineConfiguration.class, IASDocConfiguration.class,
                                     ICompcConfiguration.class );
    }

    @Test
    public void compc()
        throws Exception
    {
        checkInterfaceCompatibility( CompcMojo.class, ICompcConfiguration.class, ICommandLineConfiguration.class,
                                     IASDocConfiguration.class );
    }

    private void checkInterfaceCompatibility( Class<?> mojoClass, Class<?> must, Class<?>... bans )
    {
        Assert.assertTrue( must.isAssignableFrom( mojoClass ) );

        Method[] mustMethods = must.getMethods();
        for ( Method method : mustMethods )
        {
            Assert.assertTrue( hasMethod( mojoClass, method ) );
        }

        for ( Class<?> ban : bans )
        {
            Method[] banMethods = ban.getMethods();
            for ( Method method : banMethods )
            {
                if ( hasMethod( must, method ) )
                {
                    continue;
                }

                Assert.assertFalse( hasMethod( mojoClass, method ), "Method: " + method + " wasn't expected for "
                    + mojoClass );
            }
        }
    }

    private boolean hasMethod( Class<?> mojoClass, Method method )
    {
        try
        {
            return mojoClass.getMethod( method.getName(), method.getParameterTypes() ) != null;
        }
        catch ( Exception e )
        {
            return false;
        }
    }
}
