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
package org.sonatype.flexmojos.test.launcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;

import org.sonatype.flexmojos.test.ThreadStatus;
import org.testng.annotations.Test;

public class AsVmLauncherTest
    extends AbstractAsVmLauncherTest
{

    @Test( timeOut = 20000 )
    public void launch()
        throws Exception
    {
        launcher.start( VALID_SWF );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.DONE.equals( launcher.getStatus() ) );

        String log = launcher.getConsoleOutput();
        assertTrue( log.contains( "SWF Created!" ) );
    }

    @Test( timeOut = 20000 )
    public void stop()
        throws Exception
    {
        launcher.start( INVALID_SWF );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.RUNNING.equals( launcher.getStatus() ) );

        Thread.yield();
        Thread.sleep( 2000 );// give some extra time

        String log = launcher.getConsoleOutput();
        assertTrue( log.contains( "SWF Created!" ), "Log: " + log + " - Status: " + launcher.getStatus() );

        launcher.stop();

        Thread.yield();
        Thread.sleep( 2000 );// give some extra time

        assertEquals( ThreadStatus.ERROR, launcher.getStatus() );
        assertNotNull( launcher.getError() );
    }

    @Test( timeOut = 20000 )
    public void fakeSwf()
        throws Exception
    {
        try
        {
            launcher.start( null );
            fail();
        }
        catch ( InvalidSwfException e )
        {
            // expected
        }

        try
        {
            launcher.start( new File( "not_existing_swf_file.swf" ) );
            fail();
        }
        catch ( InvalidSwfException e )
        {
            // expected
        }
    }

}
