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
package org.sonatype.flexmojos.plugin.compiler.issues;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.sonatype.flexmojos.util.PathUtil.file;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.sonatype.flexmojos.compiler.IIncludeFile;
import org.sonatype.flexmojos.plugin.compiler.CompcMojo;
import org.sonatype.flexmojos.plugin.compiler.attributes.SimplifiablePattern;
import org.testng.annotations.Test;

public class Flexmojos345Test
{

    @Test
    public void checkFileInclusions()
        throws Exception
    {
        CompcMojo m = new CompcMojo()
        {
            @Override
            public IIncludeFile[] getIncludeFile()
            {
                includeFiles = new SimplifiablePattern();
                includeFiles.addInclude( "abc/cba/test" );
                return super.getIncludeFile();
            }

            @Override
            protected List<File> getResourcesTargetDirectories()
            {
                return Arrays.asList( file( "./target/test-classes" ) );
            }
        };

        IIncludeFile[] files = m.getIncludeFile();

        for ( IIncludeFile file : files )
        {
            assertThat( file.name(), not( containsString( "\\" ) ) );
        }
    }

}
