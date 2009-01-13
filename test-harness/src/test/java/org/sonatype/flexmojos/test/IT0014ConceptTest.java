/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.test;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.sonatype.flexmojos.tests.concept.AbstractConceptTest;
import org.testng.annotations.Test;

public class IT0014ConceptTest
    extends AbstractConceptTest
{

    // TODO still need air tests

    @Test( timeOut = 120000 )
    public void testSimpleAirApplication()
        throws Exception
    {
        standardConceptTester( "simple-air-application" );
    }

    @Test( timeOut = 120000 )
    public void testSimpleAirLibrary()
        throws Exception
    {
        standardConceptTester( "simple-air-library" );
    }

    @Test( timeOut = 120000 )
    public void testSimpleFlexApplication()
        throws Exception
    {
        standardConceptTester( "simple-flex-application" );
    }

    @Test( timeOut = 120000 )
    public void testSimpleFlexLibrary()
        throws Exception
    {
        standardConceptTester( "simple-flex-library" );
    }

    @Test( timeOut = 120000 )
    public void testFlashPlayer10()
        throws Exception
    {
        // TODO standardConceptTester( "flash-player-10" );
    }

    @Test( timeOut = 120000 )
    public void testFlex3SDK()
        throws Exception
    {
        // FIXME configure to download flex 3 sources
        // File testDir = getProject( "/flex-sdk" );
        // assertTrue( "Flex SDK source not found.  Download it from opensource.adobe.com",
        // new File( testDir, "framework" ).exists() );
        // standardConceptTester( "flex-sdk" );
    }

    @Test( timeOut = 120000 )
    public void testFlex4Gumbo()
        throws Exception
    {
        // TODO standardConceptTester( "flex4-gumbo" );
    }

    @Test( timeOut = 120000 )
    public void testHelloCaching()
        throws Exception
    {
        standardConceptTester( "hello-cachingframework" );
    }

    @Test( timeOut = 120000 )
    public void testHtmlTemplateApplication()
        throws Exception
    {
        File testDir = getProject( "/concept/html-template-application" );
        standardConceptTester( "html-template-application" );
        File template = new File( testDir, "target/html-template-application-1.0-SNAPSHOT.html" );
        assertTrue( "Html Wrapper was not generated.", template.exists() );
    }

    @Test( timeOut = 120000 )
    public void testMetadataTest()
        throws Exception
    {
        standardConceptTester( "metadata-test" );
    }

    @Test( timeOut = 120000 )
    public void testOptimizedFlexLibrary()
        throws Exception
    {
        standardConceptTester( "optimized-flex-library" );
    }

    // TODO depends on have HFCD started
    // @Test(timeOut=120000) public void testRpcHfcdSdk() throws Exception {
    // standardConceptTester("rpc-hfcd-sdk");
    // }

    @Test( timeOut = 120000 )
    public void testRuntimeCss()
        throws Exception
    {
        standardConceptTester( "runtime-css" );
    }

    @Test( timeOut = 120000 )
    public void testSimpleFlexModular()
        throws Exception
    {
        standardConceptTester( "simple-flex-modular" );
    }

    @Test( timeOut = 120000, groups = { "generator" } )
    public void testSimpleGeneration()
        throws Exception
    {
        standardConceptTester( "simple-generation" );
    }

    @Test( timeOut = 120000 )
    public void testSources()
        throws Exception
    {
        File testDir = getProject( "/concept/sources" );
        standardConceptTester( "sources" );
        File sources = new File( testDir, "target/sources-1.0-SNAPSHOT-sources.jar" );
        assertTrue( "Source file was not generated.", sources.exists() );
    }

    @Test( timeOut = 120000 )
    public void testUpdateSDK()
        throws Exception
    {
        // Need to get issue 105 to get this working
        // standardConceptTester( "updated-sdk" );
    }

    // Dont work because IT tests run under a non versioned folder
    // @Test(timeOut=120000) public void versioning() throws Exception {
    // standardConceptTester("versioning");
    // }

    @Test( timeOut = 120000 )
    public void testQuick()
        throws Exception
    {
        File testDir = getProject( "/concept/simple-flex-application" );
        // FIXME need to check time
        test( testDir, "install" );
        test( testDir, "install", "-Dquick.compile=true" );
    }

    @Test( timeOut = 120000 )
    public void testIncremental()
        throws Exception
    {
        File testDir = getProject( "/concept/simple-flex-application" );
        test( testDir, "install", "-Dincremental=true" );
    }

    @Test( timeOut = 120000 )
    public void testCompiledLocalization()
        throws Exception
    {
        File testDir = getProject( "/concept/l10n-swf/FlightReservation1" );
        test( testDir, "install" );
    }

    @Test( timeOut = 120000 )
    public void testRuntimeLocalization()
        throws Exception
    {
        File testDir = getProject( "/concept/l10n-swf/FlightReservation2" );
        test( testDir, "install" );
    }

}
