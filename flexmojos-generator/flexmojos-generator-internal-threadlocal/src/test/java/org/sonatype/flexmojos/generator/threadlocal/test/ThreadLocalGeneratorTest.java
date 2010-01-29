package org.sonatype.flexmojos.generator.threadlocal.test;

import java.io.File;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.generator.api.GenerationException;
import org.sonatype.flexmojos.generator.api.GenerationRequest;
import org.sonatype.flexmojos.generator.api.Generator;
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
        GenerationRequest request = new GenerationRequest();
        request.addClass( CLASS, new File( "target/test-classes/mxmlc.jar" ) );
        request.setTransientOutputFolder( files );
        generator.generate( request );

        Assert.assertTrue( new File( files, CLASS.replace( '.', '/' ) + ".class" ).exists() );
    }
}
