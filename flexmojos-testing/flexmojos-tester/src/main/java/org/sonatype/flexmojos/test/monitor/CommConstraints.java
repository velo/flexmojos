/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
