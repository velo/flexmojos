package flex2.compiler.util.test;

import org.sonatype.flexmojos.compiler.util.ThreadLocalToolkitHelper;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
        ThreadLocalToolkit.setLogger( null );
        Assert.assertTrue( ThreadLocalToolkitHelper.invoked );
    }

    @Test
    public void setPathResolver()
        throws Exception
    {
        ThreadLocalToolkit.setPathResolver( null );
        Assert.assertTrue( ThreadLocalToolkitHelper.invoked );
    }

}
