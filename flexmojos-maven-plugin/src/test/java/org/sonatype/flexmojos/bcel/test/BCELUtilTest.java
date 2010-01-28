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
