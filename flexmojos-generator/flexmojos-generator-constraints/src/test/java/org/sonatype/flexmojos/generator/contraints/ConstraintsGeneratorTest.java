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
package org.sonatype.flexmojos.generator.contraints;

import java.io.File;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.sonatype.flexmojos.generator.GenerationException;
import org.sonatype.flexmojos.generator.Generator;
import org.sonatype.flexmojos.generator.TestGenerationRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConstraintsGeneratorTest
{

    private Generator generator;

    @BeforeMethod
    public void setUp()
        throws Exception
    {
        DefaultPlexusContainer plexus = new DefaultPlexusContainer();
        this.generator = plexus.lookup( Generator.class, "constraints" );
    }

    @Test
    public void testGenerate()
        throws GenerationException
    {
        TestGenerationRequest request = new TestGenerationRequest();
        request.setTransientOutputFolder( new File( "./target/files" ) );
        request.addClass( "org.sonatype.flexmojos.generator.contraints.ConstraintDemo", null );
        request.setClassLoader( Thread.currentThread().getContextClassLoader() );

        generator.generate( request );
    }

}
