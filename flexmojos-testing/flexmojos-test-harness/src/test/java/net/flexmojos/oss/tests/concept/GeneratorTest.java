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

import org.testng.annotations.Test;

public class GeneratorTest
    extends AbstractConceptTest
{

    @Test( groups = { "generator" } )
    public void testGenerationGranite2()
        throws Exception
    {
        standardConceptTester( "simple-generation", "-DgeneratorArtifact=flexmojos-generator-graniteds-2.0.0",
                               "-DgeneratorToUse=graniteds2" );
    }

    @Test( groups = { "generator" } )
    public void testGenerationGranite21()
        throws Exception
    {
        standardConceptTester( "simple-generation", "-DgeneratorArtifact=flexmojos-generator-graniteds-2.1.0",
                               "-DgeneratorToUse=graniteds21" );
    }
    
    @Test( groups = { "generator" } )
    public void testGenerationGranite22()
    throws Exception
    {
        standardConceptTester( "simple-generation", "-DgeneratorArtifact=flexmojos-generator-graniteds-2.2.0",
        "-DgeneratorToUse=graniteds22" );
    }

}
