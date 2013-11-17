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

import net.flexmojos.oss.test.FMVerifier;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.zip.ZipFile;

public class AdvancedTelemetryTest
    extends AbstractConceptTest
{

    @Test( dataProvider = "flex4WithAdvancedTelemetrySupport" )
    public void flex4( String fdk )
            throws Exception
    {
        standardConceptTester( "advanced-telemetry", fdk );
    }

    public FMVerifier standardConceptTester( String conceptName, String fdk )
            throws Exception
    {
        String projectName = "/concept/" + conceptName;
        File testDir = getProjectCustom( projectName, projectName + "_" + getTestName() + "_" + fdk, fdk, (String[]) null );
        return test( testDir, "install", "-DfdkVersion=" + fdk + " -DfdkGroupId=" + getFlexGroupId(fdk));
    }

}
