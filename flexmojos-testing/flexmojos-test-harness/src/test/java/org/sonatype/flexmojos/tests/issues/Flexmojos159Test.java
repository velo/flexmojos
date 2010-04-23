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
package org.sonatype.flexmojos.tests.issues;

import org.apache.maven.it.VerificationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.text.StringContains;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos159Test
    extends AbstractIssueTest
{

    @Test
    public void localizationFromLibrary()
        throws Exception
    {
        try
        {
            testIssue( "flexmojos-159" );
            Assert.fail();
        }
        catch ( VerificationException e )
        {
            MatcherAssert.assertThat(
                                      e.getMessage(),
                                      StringContains.containsString( "Unable to resolve resource bundle \"TestBundle\" for locale \"en_US\"." ) );
            return;
        }
    }

}
