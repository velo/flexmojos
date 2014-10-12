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
package net.flexmojos.oss.generator.contraints;

import java.io.File;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import net.flexmojos.oss.generator.GenerationException;
import net.flexmojos.oss.generator.Generator;
import net.flexmojos.oss.generator.TestGenerationRequest;
import org.codehaus.plexus.PlexusConstants;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConstraintsGeneratorTest
{

    private Generator generator;

    @BeforeMethod
    public void setUp()
        throws Exception
    {
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setAutoWiring(true);
        config.setClassPathScanning(PlexusConstants.SCANNING_ON);
        DefaultPlexusContainer plexus = new DefaultPlexusContainer(config);
        this.generator = plexus.lookup( Generator.class, "constraints" );
    }

    @Test
    public void testGenerate()
        throws GenerationException
    {
        TestGenerationRequest request = new TestGenerationRequest();
        request.setTransientOutputFolder( new File( "./target/files" ) );
        request.addClass( "net.flexmojos.oss.generator.contraints.ConstraintDemo", null );
        request.setClassLoader( Thread.currentThread().getContextClassLoader() );

        generator.generate( request );
    }

}
