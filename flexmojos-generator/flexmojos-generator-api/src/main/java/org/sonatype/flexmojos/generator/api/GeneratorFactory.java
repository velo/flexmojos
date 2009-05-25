package org.sonatype.flexmojos.generator.api;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = GeneratorFactory.class )
public class GeneratorFactory
{

    @Requirement( role = Generator.class )
    private Map<String, Generator> generators;

    public Generator getGenerator( String hint )
    {
        return generators.get( hint );
    }

}
