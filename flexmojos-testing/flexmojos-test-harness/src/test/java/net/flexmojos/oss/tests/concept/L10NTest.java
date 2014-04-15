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
package net.flexmojos.oss.tests.concept;

import java.io.File;

import net.flexmojos.oss.test.FMVerifier;
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

    // TODO: This test seems to be failing since Flex 4.13 ... have to investigate.
    @Test(enabled = false)
    public void testLocalizationChain()
        throws Exception
    {
        FMVerifier v = standardConceptTester( "l10n-locale-chain" );

        final String chartsLibVersion = getArtifactVersion(getFlexFrameworkGroupId(), "charts");
        v.assertArtifactPresent( getFlexFrameworkGroupId(), "charts", chartsLibVersion, "rb.swc",
                                 "pt_BR2pt_PT" );
        v.assertArtifactNotPresent( getFlexFrameworkGroupId(), "charts", chartsLibVersion, "rb.swc",
                                    "pt_PT" );
    }

}
