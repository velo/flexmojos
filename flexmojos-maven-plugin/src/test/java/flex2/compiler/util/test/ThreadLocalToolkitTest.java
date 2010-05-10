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
