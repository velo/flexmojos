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
package net.flexmojos.oss.generator;

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
