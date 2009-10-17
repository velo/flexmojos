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
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RSLTest
    extends AbstractConceptTest
{
    @Test
    public void testInternal()
        throws Exception
    {
        Verifier verifier = standardConceptTester( "rsl", "-DconfigurationReport", "-DrslScope=internal" );
        Xpp3Dom config = getFlexConfigReport( verifier, "rsl" );
        Xpp3Dom[] internalLibraries = config.getChild( "compiler" ).getChild( "include-libraries" ).getChildren();
        Assert.assertEquals( 1, internalLibraries.length );
        Assert.assertEquals(
                             new File( internalLibraries[0].getValue() ).getCanonicalPath(),
                             new File( getProperty( "fake-repo" ),
                                       "/com/adobe/flex/framework/spark/4.0.0.8811/spark-4.0.0.8811.swc" ).getCanonicalPath() );

        Assert.assertNull( config.getChild( "runtime-shared-library-path" ) );
    }

    @Test
    public void testMerged()
        throws Exception
    {
        Verifier verifier = standardConceptTester( "rsl", "-DconfigurationReport", "-DrslScope=merged" );
        Xpp3Dom config = getFlexConfigReport( verifier, "rsl" );
        Xpp3Dom compilerConfig = config.getChild( "compiler" );
        Assert.assertNull( config.getChild( "runtime-shared-library-path" ) );
        Assert.assertNull( compilerConfig.getChild( "include-libraries" ) );

        Xpp3Dom[] mergedLibraries = compilerConfig.getChild( "library-path" ).getChildren();

        final String libraryPath =
            new File( getProperty( "fake-repo" ), "/com/adobe/flex/framework/spark/4.0.0.8811/spark-4.0.0.8811.swc" ).getCanonicalPath();
        for ( Xpp3Dom path : mergedLibraries )
        {
            if ( path.getValue().equals( libraryPath ) )
            {
                return;
            }
        }

        Assert.fail( "not found library in merged" );
    }
}
