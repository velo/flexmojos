/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.generator.granitedsv1d1d0;

import static org.granite.generator.template.StandardTemplateUris.BEAN;
import static org.granite.generator.template.StandardTemplateUris.BEAN_BASE;
import static org.granite.generator.template.StandardTemplateUris.ENTITY;
import static org.granite.generator.template.StandardTemplateUris.ENTITY_BASE;
import static org.granite.generator.template.StandardTemplateUris.ENUM;
import static org.granite.generator.template.StandardTemplateUris.INTERFACE;
import static org.granite.generator.template.StandardTemplateUris.INTERFACE_BASE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.granite.generator.Generator;
import org.granite.generator.Output;
import org.granite.generator.TemplateUri;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.DefaultAs3TypeFactory;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.PackageTranslator;
import org.granite.generator.as3.reflect.JavaEntityBean;
import org.granite.generator.as3.reflect.JavaEnum;
import org.granite.generator.as3.reflect.JavaInterface;
import org.granite.generator.as3.reflect.JavaType;
import org.granite.generator.gsp.GroovyTemplateFactory;
import org.sonatype.flexmojos.generator.GenerationRequest;

/**
 * @author edward.yakop@gmail.com
 * @since 3.2
 */
@Component( role = org.sonatype.flexmojos.generator.Generator.class, hint = "graniteds1" )
public final class GraniteDsGenerator
    extends AbstractLogEnabled
    implements org.sonatype.flexmojos.generator.Generator
{
    private static final String PREFIX_TO_REPLACE = "class:";

    private static final String SHADED_PREFIX = "class:shaded110/";

    private As3TypeFactory as3TypeFactoryImpl;

    private List<PackageTranslator> translators = new ArrayList<PackageTranslator>();

    private Map<String, File> classes;

    private String uid;

    private TemplateUri[] enumTemplateUris;

    private TemplateUri[] interfaceTemplateUris;

    private TemplateUri[] entityTemplateUris;

    private TemplateUri[] beanTemplateUris;

    private File outputDirectory;

    private File baseOutputDirectory;

    private boolean outputEnumToBaseOutputDirectory;

    private TemplateUri[] initializeEnumTemplateURIs( String[] enumTemplate )
    {
        String templateUri = get0( enumTemplate );
        if ( templateUri == null )
        {
            templateUri = ENUM.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        }
        return createTemplateUris( null, templateUri );
    }

    private TemplateUri[] initializeInterfaceTemplateURIs( String[] interfaceTemplate )
    {
        String baseTemplateUri = INTERFACE_BASE.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        String templateUri = INTERFACE.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        if ( get1( interfaceTemplate ) != null )
        {
            templateUri = get1( interfaceTemplate );
        }

        if ( get0( interfaceTemplate ) != null )
        {
            baseTemplateUri = get0( interfaceTemplate );
        }
        return createTemplateUris( baseTemplateUri, templateUri );
    }

    private TemplateUri[] initializeEntityTemplateURIs( String[] entityTemplate )
    {
        String baseTemplateUri = ENTITY_BASE.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        String templateUri = ENTITY.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        if ( get1( entityTemplate ) != null )
        {
            templateUri = get1( entityTemplate );
        }
        if ( get0( entityTemplate ) != null )
        {
            baseTemplateUri = get0( entityTemplate );
        }
        return createTemplateUris( baseTemplateUri, templateUri );
    }

    private TemplateUri[] initializeBeanTemplateURIs( String[] beanTemplate )
    {
        String baseTemplateUri = BEAN_BASE.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        String templateUri = BEAN.replaceFirst( PREFIX_TO_REPLACE, SHADED_PREFIX );
        if ( get1( beanTemplate ) != null )
        {
            templateUri = get1( beanTemplate );
        }
        if ( get0( beanTemplate ) != null )
        {
            baseTemplateUri = get0( beanTemplate );
        }
        return createTemplateUris( baseTemplateUri, templateUri );
    }

    private String get0( String[] a )
    {
        return get0Or1( a, 0 );
    }

    private String get1( String[] a )
    {
        return get0Or1( a, 1 );
    }

    private String get0Or1( String[] a, int index )
    {
        String s = a == null ? null : ( a.length < index + 1 ? null : a[index] );
        return s == null ? null : new File( s ).toURI().toString();
    }

    private TemplateUri[] createTemplateUris( String baseUri, String uri )
    {
        TemplateUri[] templateUris = new TemplateUri[baseUri == null ? 1 : 2];
        int i = 0;
        if ( baseUri != null )
        {
            templateUris[i++] = new TemplateUri( baseUri, true );
        }
        templateUris[i] = new TemplateUri( uri, false );
        return templateUris;
    }

    private String[] getTemplate( GenerationRequest configuration, String name )
    {
        String baseTemplate = configuration.getTemplates().get( "base-" + name );
        String template = configuration.getTemplates().get( name );
        return new String[] { baseTemplate, template };
    }

    public final void generate( GenerationRequest request )
    {
        String[] enumTemplate = new String[] { request.getTemplates().get( "enum-template" ) };
        enumTemplateUris = initializeEnumTemplateURIs( enumTemplate );

        String[] interfaceTemplate = getTemplate( request, "interface-template" );
        interfaceTemplateUris = initializeInterfaceTemplateURIs( interfaceTemplate );

        String[] entityTemplate = getTemplate( request, "entity-template" );
        entityTemplateUris = initializeEntityTemplateURIs( entityTemplate );

        String[] beanTemplate = getTemplate( request, "bean-template" );
        beanTemplateUris = initializeBeanTemplateURIs( beanTemplate );

        uid = request.getExtraOptions().get( "uidFieldName" );
        outputEnumToBaseOutputDirectory =
            Boolean.parseBoolean( request.getExtraOptions().get( "outputEnumToBaseOutputDirectory" ) );

        outputDirectory = request.getPersistentOutputFolder();
        baseOutputDirectory = request.getTransientOutputFolder();

        classes = request.getClasses();
        ClassLoader classLoader = request.getClassLoader();

        Generator generator = createGenerator( classLoader );

        as3TypeFactoryImpl = new DefaultAs3TypeFactory();

        int count = 0;
        for ( Map.Entry<String, File> classEntry : classes.entrySet() )
        {
            Class<?> clazz = null;
            try
            {
                clazz = classLoader.loadClass( classEntry.getKey() );
                JavaAs3Input input = new JavaAs3Input( clazz, classEntry.getValue() );
                for ( Output<?> output : generator.generate( input ) )
                {
                    if ( output.isOutdated() )
                    {
                        count++;
                    }
                }
            }
            catch ( Exception e )
            {
                getLogger().warn( "Could not generate AS3 beans for: '" + clazz + "'", e );
            }
        }

        getLogger().info( count + " files generated." );
    }

    private Generator createGenerator( ClassLoader loader )
    {
        Gas3Listener listener = new Gas3Listener( getLogger() );
        GraniteDSConfiguration configuration = new GraniteDSConfiguration( loader );
        Generator generator = new Generator( configuration );
        JavaAs3GroovyTransformer trans = new JavaAs3GroovyTransformer();
        trans.setListener( listener );
        generator.add( trans );
        return generator;
    }

    private class GraniteDSConfiguration
        implements JavaAs3GroovyConfiguration
    {
        private final GroovyTemplateFactory groovyTemplateFactory;

        private final ClassLoader classLoader;

        public GraniteDSConfiguration( ClassLoader classLoader )
        {
            this.classLoader = classLoader;
            groovyTemplateFactory = new GroovyTemplateFactory();
        }

        public String getUid()
        {
            return uid;
        }

        public boolean isGenerated( Class<?> clazz )
        {
            return classes.containsKey( clazz.getName() );
        }

        public As3TypeFactory getAs3TypeFactory()
        {
            return as3TypeFactoryImpl;
        }

        public List<PackageTranslator> getTranslators()
        {
            return translators;
        }

        public TemplateUri[] getTemplateUris( JavaType javaType )
        {
            if ( javaType instanceof JavaEnum )
            {
                return enumTemplateUris;
            }
            if ( javaType instanceof JavaInterface )
            {
                return interfaceTemplateUris;
            }
            if ( javaType instanceof JavaEntityBean )
            {
                return entityTemplateUris;
            }
            return beanTemplateUris;
        }

        public File getOutputDir( JavaAs3Input javaAs3Input )
        {
            if ( outputEnumToBaseOutputDirectory && javaAs3Input.getType().isEnum() )
            {
                return baseOutputDirectory;
            }

            return outputDirectory;
        }

        public File getBaseOutputDir( JavaAs3Input javaas3input )
        {
            return baseOutputDirectory;
        }

        public GroovyTemplateFactory getGroovyTemplateFactory()
        {
            return groovyTemplateFactory;
        }

        public ClassLoader getClassLoader()
        {
            return classLoader;
        }
    }
}
