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
package net.flexmojos.oss.tests.issues;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import com.marvinformatics.kiss.matchers.file.FileMatchers;
import org.apache.maven.it.VerificationException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Issue0152Test
    extends AbstractIssueTest
{

    @Test
    public void issue152()
        throws Exception
    {
        long off = run( false );
        long on = run( true );
        Assert.assertTrue( on < off, "loadExterns should reduce module size " + on + "/" + off );
    }

    private long run( boolean isLoadExterns )
        throws IOException, VerificationException
    {
        File testDir = getProject( "/issues/issue-0152" );
        test( testDir, "install", "-Dflex.modulesLoadExterns=" + isLoadExterns );

        File module = new File( testDir, "target/issue-0152-1.0-SNAPSHOT-anmodule.swf" );

        assertThat( module, FileMatchers.exists() );

        return module.length();
    }

}
