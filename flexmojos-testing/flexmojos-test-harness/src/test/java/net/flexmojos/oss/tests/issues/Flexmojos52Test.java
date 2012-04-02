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

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos52Test
    extends AbstractIssueTest
{

    @Test
    public void flexmojos52()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-52" );
        test( testDir, "install" );

        File target = new File( testDir, "target" );
        File acOet = new File( target, "AC_OETags.js" );
        checkFile( acOet );
        File index = new File( target, "flexmojos-52-1.0-SNAPSHOT.html" );
        checkFile( index );
    }

    private void checkFile( File file )
        throws IOException
    {
        Assert.assertTrue( file.exists() );
        String content = FileUtils.fileRead( file );
        Assert.assertFalse( content.contains( "${application}" ) );
        Assert.assertTrue( content.contains( "flexmojos-52" ) );
    }

}
