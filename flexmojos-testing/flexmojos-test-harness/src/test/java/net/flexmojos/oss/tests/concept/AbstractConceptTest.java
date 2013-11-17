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
import net.flexmojos.oss.tests.AbstractFlexMojosTests;
import org.testng.annotations.DataProvider;

public abstract class AbstractConceptTest
    extends AbstractFlexMojosTests
{

    public FMVerifier standardConceptTesterWithForcedSdk( String conceptName, String sdkVersion )
            throws Exception
    {
        File testDir = getProjectWithForcedSdk( "/concept/" + conceptName, sdkVersion );
        return test( testDir, "install", "-DfdkVersion=" + sdkVersion +
                " -DfdkGroupId=" + getFlexGroupId(sdkVersion));
    }

    public FMVerifier standardConceptTester( String conceptName, String... args )
        throws Exception
    {
        File testDir = getProject( "/concept/" + conceptName );
        return test( testDir, "install", args );
    }

    @DataProvider( name = "flex4" )
    public Object[][] flex4()
    {
        return new Object[][] { /*{ "4.8.0.1359417" },*/ { "4.9.0.1425567" }, { "4.9.1.1447119" },
                { "4.10.0.20130801" }, { "4.11.0.20131017" } };
    }

}
