package org.sonatype.flexmojos.generator.contraints;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.flexmojos.generator.api.GenerationException;
import org.sonatype.flexmojos.generator.api.GenerationRequest;
import org.sonatype.flexmojos.generator.api.Generator;

public class ConstraintsGeneratorTest
    extends PlexusTestCase
{

    private Generator generator;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.generator = lookup( Generator.class, "constraints" );
    }

    public void testGenerate()
        throws GenerationException
    {
        GenerationRequest request = new GenerationRequest();
        request.setTransientOutputFolder( new File( "./target/files" ) );
        request.addClass( "org.sonatype.flexmojos.generator.contraints.ConstraintDemo", null );
        request.setClassLoader( Thread.currentThread().getContextClassLoader() );

        generator.generate( request );
    }

}
