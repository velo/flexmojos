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
package org.sonatype.flexmojos.tests.issues;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.Test;

public class Flexmojos220Test
    extends AbstractIssueTest
{

    @Test
    public void localizedModules()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-220" ).getBasedir();
        File main = new File( baseDir, "target/flexmojos-220-1.0-SNAPSHOT.swf" );
        Assert.assertTrue( main.exists() );
        File module = new File( baseDir, "target/flexmojos-220-1.0-SNAPSHOT-module.swf" );
        Assert.assertTrue( module.exists() );
        File locale = new File( baseDir, "target/locales/flexmojos-220-1.0-SNAPSHOT-en_US.swf" );
        Assert.assertTrue( locale.exists() );
        File report = new File( baseDir, "target/flexmojos-220-1.0-SNAPSHOT-link-report.xml" );
        Assert.assertTrue( report.exists() );
    }

}
