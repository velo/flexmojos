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

public class IT0014ConceptTest
    extends AbstractMavenIntegrationTestCase
{

    public static void standardConceptTester( String conceptName )
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/" + conceptName );
        customTester( testDir, "install" );
    }

    // TODO still need air tests

    public void testSimpleAirApplication()
        throws Exception
    {
        standardConceptTester( "simple-air-application" );
    }

    public void testSimpleAirLibrary()
        throws Exception
    {
        standardConceptTester( "simple-air-library" );
    }

    public void testSimpleFlexApplication()
        throws Exception
    {
        standardConceptTester( "simple-flex-application" );
    }

    public void testSimpleFlexLibrary()
        throws Exception
    {
        standardConceptTester( "simple-flex-library" );
    }

    public void testEncrypterMojo()
        throws Exception
    {
        standardConceptTester( "encrypt-test" );
    }

    public void testFlashPlayer10()
        throws Exception
    {
        standardConceptTester( "flash-player-10" );
    }

    public void testFlex3SDK()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/flex-sdk" );
        assertTrue( "Flex SDK source not found.  Download it from opensource.adobe.com",
                    new File( testDir, "framework" ).exists() );
        standardConceptTester( "flex-sdk" );
    }

    public void testFlex4Gumbo()
        throws Exception
    {
        standardConceptTester( "flex4-gumbo" );
    }

    public void testFlexUnitExample()
        throws Exception
    {
        standardConceptTester( "flexunit-example" );
    }

    public void testHelloCaching()
        throws Exception
    {
        standardConceptTester( "hello-cachingframework" );
    }

    public void testHtmlTemplateApplication()
        throws Exception
    {
        File testDir =
            ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/html-template-application" );
        standardConceptTester( "html-template-application" );
        File template = new File( testDir, "target/html-template-application-1.0-SNAPSHOT.html" );
        assertTrue( "Html Wrapper was not generated.", template.exists() );
    }

    public void testMetadataTest()
        throws Exception
    {
        standardConceptTester( "metadata-test" );
    }

    public void testOptimizedFlexLibrary()
        throws Exception
    {
        standardConceptTester( "optimized-flex-library" );
    }

    // TODO depends on have HFCD started
    // public void testRpcHfcdSdk() throws Exception {
    // standardConceptTester("rpc-hfcd-sdk");
    // }

    public void testRuntimeCss()
        throws Exception
    {
        standardConceptTester( "runtime-css" );
    }

    public void testSimpleFlexModular()
        throws Exception
    {
        standardConceptTester( "simple-flex-modular" );
    }

    public void testSimpleGeneration()
        throws Exception
    {
        standardConceptTester( "simple-generation" );
    }

    public void testSources()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/sources" );
        standardConceptTester( "sources" );
        File sources = new File( testDir, "target/sources-1.0-SNAPSHOT-sources.jar" );
        assertTrue( "Source file was not generated.", sources.exists() );
    }

    public void testUpdateSDK()
        throws Exception
    {
        standardConceptTester( "updated-sdk" );
    }

    // Dont work because IT tests run under a non versioned folder
    // public void versioning() throws Exception {
    // standardConceptTester("versioning");
    // }

    public void testQuick()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/simple-flex-application" );
        customTester( testDir, "install" );
        customTester( testDir, "install", "-Dquick.compile=true" );
    }

    public void testIncremental()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/simple-flex-application" );
        customTester( testDir, "install", "-Dincremental=true" );
    }

    public void testCompiledLocalization()
        throws Exception
    {
        File testDir =
            ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/l10n-swf/FlightReservation1" );
        customTester( testDir, "install" );
    }

    public void testRuntimeLocalization()
        throws Exception
    {
        File testDir =
            ResourceExtractor.simpleExtractResources( MavenVerifierHelper.class, "/l10n-swf/FlightReservation2" );
        customTester( testDir, "install" );
    }

}
