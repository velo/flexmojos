/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.rvin.mojo.flexmojo.test;

import static org.testng.AssertJUnit.assertTrue;
import info.flexmojos.tests.concept.AbstractConceptTest;

import java.io.File;

import org.testng.annotations.Test;

public class IT0014ConceptTest
    extends AbstractConceptTest
{

    // TODO still need air tests

    @Test
    public void testSimpleAirApplication()
        throws Exception
    {
        standardConceptTester( "simple-air-application" );
    }

    @Test
    public void testSimpleAirLibrary()
        throws Exception
    {
        standardConceptTester( "simple-air-library" );
    }

    @Test
    public void testSimpleFlexApplication()
        throws Exception
    {
        standardConceptTester( "simple-flex-application" );
    }

    @Test
    public void testSimpleFlexLibrary()
        throws Exception
    {
        standardConceptTester( "simple-flex-library" );
    }

    @Test
    public void testEncrypterMojo()
        throws Exception
    {
        standardConceptTester( "encrypt-test" );
    }

    @Test
    public void testFlashPlayer10()
        throws Exception
    {
        standardConceptTester( "flash-player-10" );
    }

    @Test
    public void testFlex3SDK()
        throws Exception
    {
        // FIXME configure to download flex 3 sources
        // File testDir = getProject( "/flex-sdk" );
        // assertTrue( "Flex SDK source not found.  Download it from opensource.adobe.com",
        // new File( testDir, "framework" ).exists() );
        // standardConceptTester( "flex-sdk" );
    }

    @Test
    public void testFlex4Gumbo()
        throws Exception
    {
        standardConceptTester( "flex4-gumbo" );
    }

    @Test
    public void testFlexUnitExample()
        throws Exception
    {
        standardConceptTester( "flexunit-example" );
    }

    @Test
    public void testHelloCaching()
        throws Exception
    {
        standardConceptTester( "hello-cachingframework" );
    }

    @Test
    public void testHtmlTemplateApplication()
        throws Exception
    {
        File testDir = getProject( "/concept/html-template-application" );
        standardConceptTester( "html-template-application" );
        File template = new File( testDir, "target/html-template-application-1.0-SNAPSHOT.html" );
        assertTrue( "Html Wrapper was not generated.", template.exists() );
    }

    @Test
    public void testMetadataTest()
        throws Exception
    {
        standardConceptTester( "metadata-test" );
    }

    @Test
    public void testOptimizedFlexLibrary()
        throws Exception
    {
        standardConceptTester( "optimized-flex-library" );
    }

    // TODO depends on have HFCD started
    // @Test public void testRpcHfcdSdk() throws Exception {
    // standardConceptTester("rpc-hfcd-sdk");
    // }

    @Test
    public void testRuntimeCss()
        throws Exception
    {
        standardConceptTester( "runtime-css" );
    }

    @Test
    public void testSimpleFlexModular()
        throws Exception
    {
        standardConceptTester( "simple-flex-modular" );
    }

    @Test
    public void testSimpleGeneration()
        throws Exception
    {
        standardConceptTester( "simple-generation" );
    }

    @Test
    public void testSources()
        throws Exception
    {
        File testDir = getProject( "/concept/sources" );
        standardConceptTester( "sources" );
        File sources = new File( testDir, "target/sources-1.0-SNAPSHOT-sources.jar" );
        assertTrue( "Source file was not generated.", sources.exists() );
    }

    @Test
    public void testUpdateSDK()
        throws Exception
    {
        // Need to get issue 105 to get this working
        // standardConceptTester( "updated-sdk" );
    }

    // Dont work because IT tests run under a non versioned folder
    // @Test public void versioning() throws Exception {
    // standardConceptTester("versioning");
    // }

    @Test
    public void testQuick()
        throws Exception
    {
        File testDir = getProject( "/concept/simple-flex-application" );
        // FIXME need to check time
        test( testDir, "install" );
        test( testDir, "install", "-Dquick.compile=true" );
    }

    @Test
    public void testIncremental()
        throws Exception
    {
        File testDir = getProject( "/concept/simple-flex-application" );
        test( testDir, "install", "-Dincremental=true" );
    }

    @Test
    public void testCompiledLocalization()
        throws Exception
    {
        File testDir = getProject( "/concept/l10n-swf/FlightReservation1" );
        test( testDir, "install" );
    }

    @Test
    public void testRuntimeLocalization()
        throws Exception
    {
        File testDir = getProject( "/concept/l10n-swf/FlightReservation2" );
        test( testDir, "install" );
    }

}
