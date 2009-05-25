package org.sonatype.flexmojos.generator.api;

public interface Generator
{

    void generate( GenerationRequest request )
        throws GenerationException;

}
