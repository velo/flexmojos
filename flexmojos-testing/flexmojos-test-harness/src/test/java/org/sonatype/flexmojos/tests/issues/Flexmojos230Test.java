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

public class Flexmojos230Test
    extends AbstractIssueTest
{

    @Test
    public void regularAsdoc()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-230", "-Dasdoc.aggregate=false" ).getBasedir();
        File asdoc = new File( baseDir, "target/asdoc" );
        Assert.assertFalse( asdoc.exists() );

        File moduleA = new File( baseDir, "moduleA/target/asdoc" );
        Assert.assertTrue( moduleA.exists() );

        File moduleB = new File( baseDir, "moduleB/target/asdoc" );
        Assert.assertTrue( moduleB.exists() );

        File aClass = new File( moduleA, "AClass.html" );
        Assert.assertTrue( aClass.exists() );

        File bClass = new File( moduleB, "BClass.html" );
        Assert.assertTrue( bClass.exists() );

    }

    @Test
    public void aggregatedAsdoc()
        throws Exception
    {
        String baseDir = testIssue( "flexmojos-230", "-Dasdoc.aggregate=true" ).getBasedir();
        File target = new File( baseDir, "target" );
        Assert.assertTrue( target.exists() );

        File aClass = new File( target, "asdoc/AClass.html" );
        Assert.assertTrue( aClass.exists() );

        File bClass = new File( target, "asdoc/BClass.html" );
        Assert.assertTrue( bClass.exists() );

        File moduleA = new File( baseDir, "moduleA/target/asdoc" );
        Assert.assertFalse( moduleA.exists() );

        File moduleB = new File( baseDir, "moduleB/target/asdoc" );
        Assert.assertFalse( moduleB.exists() );
    }

}
