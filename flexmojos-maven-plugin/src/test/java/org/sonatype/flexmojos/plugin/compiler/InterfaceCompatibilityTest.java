package org.sonatype.flexmojos.plugin.compiler;

import java.lang.reflect.Method;

import org.sonatype.flexmojos.compiler.IASDocConfiguration;
import org.sonatype.flexmojos.compiler.ICommandLineConfiguration;
import org.sonatype.flexmojos.compiler.ICompcConfiguration;
import org.sonatype.flexmojos.plugin.compiler.AsdocMojo;
import org.sonatype.flexmojos.plugin.compiler.CompcMojo;
import org.sonatype.flexmojos.plugin.compiler.MxmlcMojo;
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
