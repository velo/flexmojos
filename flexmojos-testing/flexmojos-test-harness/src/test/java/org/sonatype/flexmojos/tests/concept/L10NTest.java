/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.tests.concept;

import java.io.File;

import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class L10NTest
    extends AbstractConceptTest
{

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

    @Test
    public void testLocalizedLibraryAndApplication()
        throws Exception
    {
        standardConceptTester( "l10n-swc-swf" );
    }

    @Test
    public void testLocalizationChain()
        throws Exception
    {
        FMVerifier v = standardConceptTester( "l10n-locale-chain" );
        v.assertArtifactPresent( "com.adobe.flex.framework", "charts", getFlexSDKVersion(), "rb.swc",
                                 "pt_BR2pt_PT" );
        v.assertArtifactNotPresent( "com.adobe.flex.framework", "charts", getFlexSDKVersion(), "rb.swc",
                                    "pt_PT" );
    }

}
