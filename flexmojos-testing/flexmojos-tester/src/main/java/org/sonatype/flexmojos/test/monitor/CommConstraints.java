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
package org.sonatype.flexmojos.test.monitor;

public class CommConstraints
{

    public static final String END_OF_TEST_RUN = "<endOfTestRun/>";

    public static final String END_OF_TEST_SUITE = "</testsuite>";

    public static final String ACK_OF_TEST_RESULT = "<endOfTestRunAck/>";

    public static final char NULL_BYTE = '\u0000';

    public static final String STATUS = "Server Status";

    public static final String OK = "OK";

    public static final String FINISHED = "FINISHED";

    public static final char EOL = '\n';

}
