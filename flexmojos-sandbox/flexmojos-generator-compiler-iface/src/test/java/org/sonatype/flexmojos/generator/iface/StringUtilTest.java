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
package org.sonatype.flexmojos.generator.iface;

import java.util.Arrays;

import junit.framework.TestCase;

public class StringUtilTest
    extends TestCase
{

    public void testPrefixRemoval()
    {
        String a = "abcCba";
        assertEquals( "Cba", StringUtil.removePrefix( a ) );
        a = "abc3Cba";
        assertEquals( "Cba", StringUtil.removePrefix( a ) );
    }

    public void testSplit()
    {
        String a = "abcCba";
        assertEquals( Arrays.toString( new String[] { "abc", "cba" } ),
                      Arrays.toString( StringUtil.splitCamelCase( a ) ) );
    }

}
