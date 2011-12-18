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
package org.sonatype.flexmojos.tests.concept;

import java.io.File;

import org.sonatype.flexmojos.test.FMVerifier;
import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
import org.testng.annotations.DataProvider;

public abstract class AbstractConceptTest
    extends AbstractFlexMojosTests
{

    public FMVerifier standardConceptTester( String conceptName, String... args )
        throws Exception
    {
        File testDir = getProject( "/concept/" + conceptName );
        return test( testDir, "install", args );
    }

    @DataProvider( name = "flex3" )
    public Object[][] flex3()
    {
        return new Object[][] { { "3.0.0.477" }, { "3.1.0.2710" }, { "3.2.0.3958" }, { "3.3.0.4852" },
            { "3.4.0.9271" }, { "3.5.a.12683" }, { "3.6.0.16995" } };
    }

    @DataProvider( name = "flex4" )
    public Object[][] flex4()
    {
        return new Object[][] { { "4.0.0.14159" }, { "4.0.a.14159" }, { "4.1.0.16076" }, { "4.5.0.20967" } };
    }

}
