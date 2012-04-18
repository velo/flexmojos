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
package net.flexmojos.oss.test;

import java.io.File;

import org.apache.maven.it.VerificationException;
import net.flexmojos.oss.tests.AbstractFlexMojosTests;
import org.testng.annotations.Test;

public class IT0013IssuesTest
    extends AbstractFlexMojosTests
{

    public void testIssue( String issueNumber )
        throws Exception
    {
        File testDir = getProject( "/issues/" + issueNumber );
        test( testDir, "install" );
    }

    @Test
    public void issue11()
        throws Exception
    {
        testIssue( "issue-0011" );
    }

    @Test( expectedExceptions = { VerificationException.class } )
    public void issue14()
        throws Exception
    {
        testIssue( "issue-0014" );
    }

    @Test
    public void issue29()
        throws Exception
    {
        testIssue( "issue-0029" );
    }

    // A wierd but on this tests
    // @Test(timeOut=120000) public void issue43() throws Exception {
    // File testDir = ResourceExtractor.simpleExtractResources(getClass(),
    // "/issues/issue-0014");
    // List<String> args = new ArrayList<String>();
    // args.add("-Dmaven.test.failure.ignore=true");
    // test(testDir, "info.rvin.itest.issues", "issue-0014",
    // "1.0-SNAPSHOT", "swf", "install", args);
    // }

    @Test
    public void issue53()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0014" );
        test( testDir, "install", "-Dmaven.test.skip=true" );

        test( testDir, "install", "-DskipTests=true" );
    }

    @Test( groups = { "generator" } )
    public void issue61()
        throws Exception
    {
        testIssue( "issue-0061" );
    }

    @Test
    public void issue68()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0068" );
        test( testDir, "net.flexmojos.oss:flexmojos-maven-plugin:" + getProperty( "version" ) + ":asdoc" );
    }

    @Test
    public void issue70()
        throws Exception
    {
        testIssue( "issue-0070" );
    }

}
