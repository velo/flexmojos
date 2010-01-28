package org.sonatype.flexmojos.bcel.test;

import java.io.File;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.sonatype.flexmojos.bcel.BCELUtil;
import org.sonatype.flexmojos.compiler.FlexCompiler;
import org.sonatype.flexmojos.utilities.FlexmojosCompilerAPIHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BCELUtilTest
{
    @Test
    public void initializeCompiler()
        throws Exception
    {
        PlexusContainer plexus = new DefaultPlexusContainer();
        File mxmlcJar =
            new File( "C:/Users/Seven/.m2/repository/com/adobe/flex/compiler/mxmlc/4.0.0.13555/mxmlc-4.0.0.13555.jar" );
        Object so = BCELUtil.initializeCompiler( plexus, mxmlcJar );
        FlexCompiler compiler = BCELUtil.initializeCompiler( plexus, mxmlcJar );
        compiler.compileSwc( null );

        Assert.assertTrue( FlexmojosCompilerAPIHelper.invoked );
    }

}
