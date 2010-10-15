package org.sonatype.flexmojos.generator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.Map;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.testng.annotations.Test;

public class GeneratorFactoryTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void testFactory()
        throws Exception
    {
        DefaultPlexusContainer plexus = new DefaultPlexusContainer();

        GeneratorFactory factory = plexus.lookup( GeneratorFactory.class );

        Generator generator = factory.getGenerator( "dummy" );
        assertThat( generator, notNullValue() );
        assertThat( generator, instanceOf( DummyGenerator.class ) );

        TestGenerationRequest request = new TestGenerationRequest();
        request.addClass( "classA", mock( File.class ) );
        request.addClass( "classC", mock( File.class ) );
        request.addClass( "classB", mock( File.class ) );
        request.setClasses( request.getClasses() );
        request.setClassLoader( mock( ClassLoader.class ) );
        request.addExtraOption( "A", "1" );
        request.addExtraOption( "B", "2" );
        request.addExtraOption( "C", "3" );
        request.setExtraOptions( request.getExtraOptions() );
        request.setLogger( mock( GeneratorLogger.class ) );
        request.setPersistentOutputFolder( mock( File.class ) );
        request.setTransientOutputFolder( mock( File.class ) );
        request.setTranslators( new String[] { "Something", "here", "and", "there" } );
        request.setTemplates( mock( Map.class ) );
        try
        {
            generator.generate( request );
        }
        catch ( GenerationException e )
        {
            assertThat( e.getMessage(), nullValue() );
        }

        DummyGenerator dGen = (DummyGenerator) generator;
        assertThat( dGen.getLogger(), equalTo( request.getLogger() ) );
        assertThat( dGen.getClasses(), equalTo( request.getClasses() ) );
        assertThat( dGen.getClassLoader(), equalTo( request.getClassLoader() ) );
        assertThat( dGen.getExtraOptions(), equalTo( request.getExtraOptions() ) );
        assertThat( dGen.getPersistentOutputFolder(), equalTo( request.getPersistentOutputFolder() ) );
        assertThat( dGen.getTemplate(), equalTo( request.getTemplates() ) );
        assertThat( dGen.getTransientOutputFolder(), equalTo( request.getTransientOutputFolder() ) );
        assertThat( dGen.getTranslators(), equalTo( request.getTranslators() ) );
    }
}
