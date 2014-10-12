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
package net.flexmojos.oss.generator.iface;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.util.FileUtils;
import net.flexmojos.oss.generator.GenerationException;
import net.flexmojos.oss.generator.Generator;
import net.flexmojos.oss.generator.TestGenerationRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import flex2.tools.ASDocConfiguration;
import flex2.tools.CompcConfiguration;
import flex2.tools.DigestRootConfiguration;
import flex2.tools.Optimizer.OptimizerConfiguration;
import flex2.tools.ToolsConfiguration;

@SuppressWarnings( "restriction" )
public class IFaceGeneratorTest
{

    private Generator generator;

    private File files;

    @BeforeMethod
    public void setUp()
        throws Exception
    {
        ContainerConfiguration config = new DefaultContainerConfiguration();
        config.setAutoWiring(true);
        config.setClassPathScanning(PlexusConstants.SCANNING_ON);
        DefaultPlexusContainer plexus = new DefaultPlexusContainer(config);

        this.generator = plexus.lookup( Generator.class, "internal-ifaces" );

        files = new File( "./target/files" );
        FileUtils.forceDelete( files );

    }

    @Test
    public void testGenerate()
        throws GenerationException, IOException
    {
        TestGenerationRequest request = new TestGenerationRequest();
        request.setTransientOutputFolder( files );
        request.addClass( CompcConfiguration.class.getName(), null );
        request.addClass( ToolsConfiguration.class.getName(), null );
        request.addClass( ASDocConfiguration.class.getName(), null );
        request.addClass( OptimizerConfiguration.class.getName(), null );
        request.addClass( DigestRootConfiguration.class.getName(), null );
        System.out.println( request.getClasses() );
        request.setClassLoader( Thread.currentThread().getContextClassLoader() );

        generator.generate( request );

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager( null, null, null );

        Iterable<? extends JavaFileObject> compilationUnits1 =
            fileManager.getJavaFileObjects( new File( files, "net/flexmojos/oss/compiler" ).listFiles() );
        assertTrue( compiler.getTask( null, fileManager, null, null, null, compilationUnits1 ).call(),
                    "Problems compiling files" );

        fileManager.close();
    }

}
