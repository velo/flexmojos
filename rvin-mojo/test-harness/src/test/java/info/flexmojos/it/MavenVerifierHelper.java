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
package info.flexmojos.it;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.IOUtil;

public class MavenVerifierHelper
{

    public static void customTester( File testDir, String goal, String... args )
        throws Exception
    {
        Verifier verifier = new Verifier( testDir.getAbsolutePath(), false );
        verifier.resetStreams();
        verifier.setCliOptions( Arrays.asList( args ) );
        verifier.executeGoal( goal );
        try
        {
            verifier.verifyErrorFreeLog();
        }
        catch ( VerificationException e )
        {
            System.out.println();
            System.out.println( "Test folder " + testDir.getAbsolutePath() );
            try
            {
                File logFile = new File( verifier.getBasedir(), "log.txt" );
                String log = IOUtil.toString( new FileReader( logFile ) );
                System.out.println( log );
            }
            catch ( Throwable t )
            {
                // skip
                System.out.println( "Unable to print maven log" );
            }
            throw e;
        }
    }
}
