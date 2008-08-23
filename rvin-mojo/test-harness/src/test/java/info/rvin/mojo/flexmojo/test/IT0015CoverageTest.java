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
package info.rvin.mojo.flexmojo.test;

import static info.flexmojos.it.MavenVerifierHelper.customTester;
import info.flexmojos.it.MavenVerifierHelper;

import java.io.File;

import org.apache.maven.integrationtests.AbstractMavenIntegrationTestCase;
import org.apache.maven.it.util.ResourceExtractor;

public class IT0015CoverageTest
    extends AbstractMavenIntegrationTestCase
{

    public static void standardConceptTester( String coverageName )
        throws Exception
    {
        File testDir =
            ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/coverage/" + coverageName );
        customTester( testDir, "install" );
    }

    public void testSourceFileResolver()
        throws Exception
    {
        standardConceptTester( "source-file-resolver" );
    }

    public void testAsdocInclusionExclusion()
        throws Exception
    {
        File testDir =
            ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/coverage/asdoc-inclusion-exclusion" );
        customTester( testDir, "asdoc:asdoc" );
        File vermelho = new File( testDir, "target/asdoc/Vermelho.html" );
        assertFalse( "Should not generate Vermelho.html.", vermelho.exists() );
        File amarelo = new File( testDir, "target/asdoc/Amarelo.html" );
        assertFalse( "Should not generate Amarelo.html.", amarelo.exists() );
    }

    public void testFlexUnitReport()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/flexunit-example" );
        customTester( testDir, "site:site" );
        File asdoc = new File( testDir, "/target/site/asdocs" );
        assertTrue( asdoc.isDirectory() );
    }

    public void testHtmlwrapperTemplates()
        throws Exception
    {
        File testDir =
            ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/coverage/htmlwrapper-templates" );
        customTester( testDir, "package" );
        File folder = new File( testDir, "folder/target/htmlwrapper-templates-folder-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper" + folder.getAbsolutePath(), folder.exists() );
        File zip = new File( testDir, "zip/target/htmlwrapper-templates-zip-1.0-SNAPSHOT.html" );
        assertTrue( "Should generate htmlwrapper " + zip.getAbsolutePath(), zip.exists() );
    }

    public void testDefines()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/issues/issue-0068" );
        customTester( testDir, "install" );

    }

    public void testCompilationOptions()
        throws Exception
    {
        standardConceptTester( "compilation-options" );
    }

}
