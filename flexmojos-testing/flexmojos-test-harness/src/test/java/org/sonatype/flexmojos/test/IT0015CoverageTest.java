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
package org.sonatype.flexmojos.test;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.sonatype.flexmojos.tests.coverage.AbstractCoverageTest;
import org.testng.annotations.Test;

public class IT0015CoverageTest
    extends AbstractCoverageTest
{

    @Test
    public void testAsdocInclusionExclusion()
        throws Exception
    {
        File testDir = getProject( "/coverage/asdoc-inclusion-exclusion" );
        test( testDir, "org.sonatype.flexmojos:flexmojos-maven-plugin:" + getProperty( "version" ) + ":asdoc" );
        File vermelho = new File( testDir, "target/asdoc/Vermelho.html" );
        assertFalse( "Should not generate Vermelho.html.", vermelho.exists() );
        File amarelo = new File( testDir, "target/asdoc/Amarelo.html" );
        assertFalse( "Should not generate Amarelo.html.", amarelo.exists() );
    }

    @Test
    public void testFlexUnitReport()
        throws Exception
    {
        File testDir = getProject( "/concept/flexunit-example" );
        test( testDir, "site:site" );
        File asdoc = new File( testDir, "/target/site/asdoc" );
        assertTrue( asdoc.isDirectory() );
    }

    @Test
    public void testDefines()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0068" );
        test( testDir, "install" );

    }

    @Test
    public void testCompilationOptions()
        throws Exception
    {
        standardCoverageTester( "compilation-options" );
    }

}
