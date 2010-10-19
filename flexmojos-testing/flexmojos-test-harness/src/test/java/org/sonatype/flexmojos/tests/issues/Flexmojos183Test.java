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
package org.sonatype.flexmojos.tests.issues;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.sonatype.flexmojos.matcher.file.FileMatcher;
import org.sonatype.flexmojos.test.FMVerifier;
import org.testng.annotations.Test;

public class Flexmojos183Test
    extends AbstractIssueTest
{

    @Test( enabled = false )
    public void font2swf()
        throws Exception
    {
        FMVerifier v = testIssue( "flexmojos-183", "-DisIt=true" );
        String dir = v.getBasedir();

        File target = new File( dir, "target" );
        File main = new File( target, "flexmojos-183-1.0-SNAPSHOT.swf" );

        assertSeftExit( main, 3539, v );

        File font = new File( target, "classes/myFont.swf" );
        assertThat( font, FileMatcher.exists() );
    }
}
