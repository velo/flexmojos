package org.sonatype.flexmojos.generator.iface;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.generator.GenerationException;
import org.sonatype.flexmojos.generator.Generator;
import org.sonatype.flexmojos.generator.TestGenerationRequest;
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
        DefaultPlexusContainer plexus = new DefaultPlexusContainer();

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
            fileManager.getJavaFileObjects( new File( files, "org/sonatype/flexmojos/compiler" ).listFiles() );
        assertTrue( compiler.getTask( null, fileManager, null, null, null, compilationUnits1 ).call(),
                    "Problems compiling files" );

        fileManager.close();
    }

}
