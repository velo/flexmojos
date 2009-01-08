/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.generator;

import info.rvin.flexmojos.utilities.MavenUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;
import org.granite.generator.Generator;
import org.granite.generator.Output;
import org.granite.generator.TemplateUri;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.DefaultAs3TypeFactory;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.PackageTranslator;
import org.granite.generator.as3.reflect.JavaEntityBean;
import org.granite.generator.as3.reflect.JavaEnum;
import org.granite.generator.as3.reflect.JavaInterface;
import org.granite.generator.as3.reflect.JavaType;
import org.granite.generator.gsp.GroovyTemplateFactory;
import org.granite.generator.template.StandardTemplateUris;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal generate
 * @phase generate-sources
 * @requiresDependencyResolution
 */
public class GeneratorMojo
    extends AbstractMojo
    implements JavaAs3GroovyConfiguration
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * File to generate as3 file. If not defined assumes all classes must be included
     * 
     * @parameter
     */
    private String[] includeClasses;

    /**
     * File to exclude from as3 generation. If not defined, assumes no exclusions
     * 
     * @parameter
     */
    private String[] excludeClasses;

    /**
     * File to include as output from as3 generation. If not defined, assumes all classes are included in the output
     * 
     * @parameter
     */
    private String[] outputClasses;

    /**
     * @parameter expression="${project.build}"
     * @required
     * @readonly
     */
    protected Build build;

    /**
     * @parameter default-value="${project.build.sourceDirectory}"
     */
    private File outputDirectory;

    /**
     * @parameter default-value="${project.build.directory}/generated-sources/flex-mojos"
     */
    private File baseOutputDirectory;

    /**
     * @parameter
     */
    private String uid = "uid";

    /**
     * @parameter
     */
    private String[] entityTemplate;

    /**
     * @parameter
     */
    private String[] interfaceTemplate;

    /**
     * @parameter
     */
    private String[] beanTemplate;

    /**
     * @parameter
     */
    private String[] enumTemplate;

    /**
     * @parameter default-value="false"
     */
    private boolean useTransitiveDependencies;

    /**
     * Controls whether or not enum classes are output to the baseOutputDirectory (true) or the outputDirectory (false)
     * 
     * @parameter default-value="false"
     */
    private boolean outputEnumToBaseOutputDirectory;

    /**
     * internal properties
     */
    private Gas3Listener listener;

    private As3TypeFactory as3TypeFactoryImpl;

    private List<PackageTranslator> translators = new ArrayList<PackageTranslator>();

    private Map<String, File> classes;

    private ClassLoader loader;

    private GroovyTemplateFactory groovyTemplateFactory = null;

    private TemplateUri[] entityTemplateUris = null;

    private TemplateUri[] interfaceTemplateUris = null;

    private TemplateUri[] beanTemplateUris = null;

    private TemplateUri[] enumTemplateUris = null;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    /**
     * @component
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * @component
     */
    protected MavenProjectBuilder mavenProjectBuilder;

    /**
     * Local repository to be used by the plugin to resolve dependencies.
     * 
     * @parameter expression="${localRepository}"
     */
    protected ArtifactRepository localRepository;

    /**
     * List of remote repositories to be used by the plugin to resolve dependencies.
     * 
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    @SuppressWarnings( "unchecked" )
    protected List remoteRepositories;

    public void execute()
        throws MojoExecutionException
    {
        getLog().info(
                       "Flex-mojos " + MavenUtils.getFlexMojosVersion()
                           + " - GNU GPL License (NO WARRANTY) - See COPYRIGHT file" );

        setUp();

        List<File> jarDependencies = getJarDependencies();
        if ( jarDependencies.isEmpty() )
        {
            getLog().warn( "No jar dependencies found." );
            return;
        }

        try
        {
            classes = getClasses( jarDependencies );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error on classes resolve", e );
        }

        try
        {
            // create a new classloading space
            ClassWorld world = new ClassWorld();

            // use the existing ContextClassLoader in a realm of the classloading space
            ClassRealm realm =
                world.newRealm( "plugin.flex-mojos.generator", Thread.currentThread().getContextClassLoader() );

            // create another realm for just the dependency jars and make
            // sure it is in a child-parent relationship with the current ContextClassLoader
            ClassRealm gas3GeneratorRealm = realm.createChildRealm( "gas3Generator" );

            // add all the jars to the new child realm
            for ( URL url : getUrls( jarDependencies ) )
                gas3GeneratorRealm.addConstituent( url );

            loader = gas3GeneratorRealm.getClassLoader();
            Thread.currentThread().setContextClassLoader( gas3GeneratorRealm.getClassLoader() );
        }
        catch ( MalformedURLException e )
        {
            throw new MojoExecutionException( "Unable to get dependency URL", e );
        }
        catch ( DuplicateRealmException e )
        {
            throw new MojoExecutionException( "Unable to create new class loading realm", e );
        }

        setupTemplateUris();

        Generator generator = getGenerator( loader );

        as3TypeFactoryImpl = new DefaultAs3TypeFactory();

        int count = 0;
        for ( Map.Entry<String, File> classEntry : classes.entrySet() )
        {
            Class<?> clazz = null;
            try
            {
                clazz = loader.loadClass( classEntry.getKey() );
                JavaAs3Input input = new JavaAs3Input( clazz, classEntry.getValue() );
                for ( Output<?> output : generator.generate( input ) )
                {
                    if ( output.isOutdated() )
                        count++;
                }
            }
            catch ( Exception e )
            {
                getLog().warn( "Could not generate AS3 beans for: '" + clazz + "'", e );
            }
        }
        getLog().info( count + " files generated." );
    }

    private URL[] getUrls( List<File> jarDependencies )
        throws MalformedURLException
    {
        URL[] urls = new URL[jarDependencies.size()];
        for ( int i = 0; i < jarDependencies.size(); i++ )
        {
            urls[i] = jarDependencies.get( i ).toURL();
        }
        return urls;
    }

    @SuppressWarnings( "unchecked" )
    private List<File> getJarDependencies()
    {
        List<File> jarDependencies = new ArrayList<File>();
        final Collection<Artifact> artifacts;
        if ( useTransitiveDependencies )
        {
            artifacts = project.getArtifacts();
        }
        else
        {
            artifacts = project.getDependencyArtifacts();
        }

        for ( Artifact artifact : artifacts )
        {
            if ( "jar".equals( artifact.getType() ) )
            {
                try
                {
                    resolver.resolveAlways( artifact, remoteRepositories, localRepository );
                }
                catch ( AbstractArtifactResolutionException e )
                {
                    getLog().warn( "Dependency file not found: " + artifact );
                    getLog().debug( e );
                    continue;
                }

                File file = artifact.getFile();
                if ( file != null && file.exists() )
                {
                    jarDependencies.add( file );
                }
            }
        }
        return jarDependencies;
    }

    private void setupTemplateUris()
    {
        String baseTemplateUri = null;
        String templateUri = StandardTemplateUris.ENUM;
        if ( get0( enumTemplate ) != null )
        {
            templateUri = get0( enumTemplate );
        }
        enumTemplateUris = createTemplateUris( baseTemplateUri, templateUri );

        // Interface templates.
        baseTemplateUri = StandardTemplateUris.INTERFACE_BASE;
        templateUri = StandardTemplateUris.INTERFACE;
        if ( get1( interfaceTemplate ) != null )
        {
            templateUri = get1( interfaceTemplate );
        }
        if ( get0( interfaceTemplate ) != null )
        {
            baseTemplateUri = get0( interfaceTemplate );
        }
        interfaceTemplateUris = createTemplateUris( baseTemplateUri, templateUri );

        // Entity templates.
        baseTemplateUri = StandardTemplateUris.ENTITY_BASE;
        templateUri = StandardTemplateUris.ENTITY;
        if ( get1( entityTemplate ) != null )
        {
            templateUri = get1( entityTemplate );
        }
        if ( get0( entityTemplate ) != null )
        {
            baseTemplateUri = get0( entityTemplate );
        }
        entityTemplateUris = createTemplateUris( baseTemplateUri, templateUri );

        // Other bean templates.
        baseTemplateUri = StandardTemplateUris.BEAN_BASE;
        templateUri = StandardTemplateUris.BEAN;
        if ( get1( beanTemplate ) != null )
        {
            templateUri = get1( beanTemplate );
        }
        if ( get0( beanTemplate ) != null )
        {
            baseTemplateUri = get0( beanTemplate );
        }
        beanTemplateUris = createTemplateUris( baseTemplateUri, templateUri );

    }

    private Generator getGenerator( ClassLoader loader )
        throws MojoExecutionException
    {
        this.listener = new Gas3Listener( getLog() );
        Generator generator = new Generator( this );
        generator.add( new Gas3GroovyTransformer( this, this.listener, outputClasses ) );
        return generator;
    }

    private Map<String, File> getClasses( List<File> jarDependencies )
        throws IOException
    {
        Map<String, File> classes = new HashMap<String, File>();
        for ( File file : jarDependencies )
        {
            JarInputStream jar = new JarInputStream( new FileInputStream( file ) );

            JarEntry jarEntry;
            while ( true )
            {
                jarEntry = jar.getNextJarEntry();

                if ( jarEntry == null )
                {
                    break;
                }

                String className = jarEntry.getName();

                if ( jarEntry.isDirectory() || !className.endsWith( ".class" ) )
                {
                    continue;
                }

                className = className.replace( '/', '.' );
                className = className.substring( 0, className.length() - 6 );

                if ( matchWildCard( className, includeClasses ) && !matchWildCard( className, excludeClasses ) )
                {
                    classes.put( className, file );
                }
            }
        }

        return classes;
    }

    private boolean matchWildCard( String className, String[] wildCards )
    {
        if ( wildCards == null )
        {
            return false;
        }

        for ( String wildCard : wildCards )
        {
            if ( FilenameUtils.wildcardMatch( className, wildCard ) )
                return true;
        }

        return false;
    }

    private void setUp()
        throws MojoExecutionException
    {
        if ( includeClasses == null )
        {
            includeClasses = new String[] { "*" };
        }

        if ( !outputDirectory.exists() )
        {
            outputDirectory.mkdirs();
        }

        String outputPath = outputDirectory.getAbsolutePath();
        if ( !project.getCompileSourceRoots().contains( outputPath ) )
        {
            project.addCompileSourceRoot( outputPath );
        }

        if ( !baseOutputDirectory.exists() )
        {
            baseOutputDirectory.mkdirs();
        }
        String baseOutputPath = baseOutputDirectory.getAbsolutePath();
        if ( !project.getCompileSourceRoots().contains( baseOutputPath ) )
        {
            project.addCompileSourceRoot( baseOutputPath );
        }

    }

    private String get0( String[] a )
    {

        return this.get0Or1( a, 0 );
    }

    private String get1( String[] a )
    {

        return this.get0Or1( a, 1 );
    }

    private String get0Or1( String[] a, int index )
    {

        String s = a == null ? null : ( a.length < index + 1 ? null : a[index] );

        return s == null ? null : new File( s ).toURI().toString();
    }

    public As3TypeFactory getAs3TypeFactory()
    {
        return as3TypeFactoryImpl;
    }

    public File getBaseOutputDir( JavaAs3Input javaas3input )
    {
        return baseOutputDirectory;
    }

    public File getOutputDir( JavaAs3Input javaas3input )
    {
        if ( outputEnumToBaseOutputDirectory && javaas3input.getType().isEnum() )
            return baseOutputDirectory;
        return outputDirectory;
    }

    public TemplateUri[] getTemplateUris( JavaType javaType )
    {
        if ( javaType instanceof JavaEnum )
            return enumTemplateUris;
        if ( javaType instanceof JavaInterface )
            return interfaceTemplateUris;
        if ( javaType instanceof JavaEntityBean )
            return entityTemplateUris;
        return beanTemplateUris;
    }

    public List<PackageTranslator> getTranslators()
    {
        return translators;
    }

    public String getUid()
    {
        return uid;
    }

    public boolean isGenerated( Class<?> clazz )
    {
        return classes.containsKey( clazz.getName() );
    }

    public GroovyTemplateFactory getGroovyTemplateFactory()
    {
        if ( groovyTemplateFactory == null )
            groovyTemplateFactory = new GroovyTemplateFactory();
        return groovyTemplateFactory;
    }

    public ClassLoader getClassLoader()
    {
        return loader;
    }

    private TemplateUri[] createTemplateUris( String baseUri, String uri )
    {
        TemplateUri[] templateUris = new TemplateUri[baseUri == null ? 1 : 2];
        int i = 0;
        if ( baseUri != null )
            templateUris[i++] = new TemplateUri( baseUri, true );
        templateUris[i] = new TemplateUri( uri, false );
        return templateUris;
    }
}
