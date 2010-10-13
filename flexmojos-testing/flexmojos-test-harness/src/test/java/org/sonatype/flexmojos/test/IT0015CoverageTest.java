/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
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
    public void testAsdocReport()
        throws Exception
    {
        File testDir = getProject( "/concept/flexunit-example" );
        test( testDir, siteGoal() );
        File asdoc = new File( testDir, "target/site/asdoc" );
        assertTrue( asdoc.isDirectory() );
    }

    @Test
    public void testHtmlwrapperTemplates()
        throws Exception
    {
        File testDir = getProject( "/coverage/htmlwrapper-templates" );
        test( testDir, "package" );
        File folder = new File( testDir, "folder/target/htmlwrapper-templates-folder-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper" + folder.getAbsolutePath(), folder.exists() );
        File zip = new File( testDir, "zip/target/htmlwrapper-templates-zip-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper " + zip.getAbsolutePath(), zip.exists() );
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
