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

import org.apache.maven.it.Verifier;
import org.testng.FileAssert;
import org.testng.annotations.Test;

public class OptimizationTest
    extends AbstractConceptTest
{

    @Test( groups = { "optimizer" } )
    public void testOptimizedFlexLibrary()
        throws Exception
    {
        Verifier v = standardConceptTester( "optimized-flex-library" );
        v.assertArtifactPresent( "info.rvin.itest", "optimized-flex-library", "1.0-SNAPSHOT", "swc" );
        v.assertArtifactPresent( "info.rvin.itest", "optimized-flex-library", "1.0-SNAPSHOT", "swf" );
    }

    @Test( groups = { "optimizer" } )
    public void testOptimizedApplication()
        throws Exception
    {
        Verifier v = standardConceptTester( "optimized-application" );
        v.assertArtifactPresent( "info.rvin.itest", "optimized-application", "1.0-SNAPSHOT", "swf" );
        File path = new File( v.getArtifactPath( "info.rvin.itest", "optimized-application", "1.0-SNAPSHOT", "swf" ) );
        FileAssert.assertFile( new File( path.getParentFile(), "optimized-application-1.0-SNAPSHOT-original.swf" ) );
    }

}
