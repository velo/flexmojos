/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.tests;

import org.testng.annotations.Test;

public class IT0091HelloWordTest
    extends AbstractFlexMojosTests
{

    @Test( timeOut = 120000 )
    public void helloWordTest()
        throws Exception
    {
        // File testDir = getProject( "/issues/issue-0017" );
        // test( testDir, "site" );
        //
        // File asdoc = new File( testDir, "target/asdoc" );
        // assertTrue( "asdoc directory must exist", asdoc.isDirectory() );
        test( getProject( "intro/hello-world" ), "install" );
    }

    @Test( timeOut = 120000 )
    public void helloWordNoInherit()
        throws Exception
    {
        test( getProject( "intro/hello-world-no-inherit" ), "install" );
    }

}
