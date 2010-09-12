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
package org.sonatype.flexmojos.generator.threadlocal.test;

import java.io.File;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.generator.GenerationException;
import org.sonatype.flexmojos.generator.Generator;
import org.sonatype.flexmojos.generator.TestGenerationRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ThreadLocalGeneratorTest

{
    private static final String CLASS = "flex2.compiler.util.ThreadLocalToolkit";

    private Generator generator;

    private File files;

    @BeforeClass
    protected void setUp()
        throws Exception
    {
        DefaultPlexusContainer plexus = new DefaultPlexusContainer();

        this.generator = plexus.lookup( Generator.class, "thread-local" );

        files = new File( "./target/files" );
        FileUtils.forceDelete( files );

    }

    @Test
    public void generate()
        throws GenerationException
    {
        TestGenerationRequest request = new TestGenerationRequest();
        request.addClass( CLASS, new File( "target/test-classes/mxmlc.jar" ) );
        request.setTransientOutputFolder( files );
        generator.generate( request );

        Assert.assertTrue( new File( files, CLASS.replace( '.', '/' ) + ".class" ).exists() );
    }
}
