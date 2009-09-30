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
package org.sonatype.flexmojos.compiler;

import static org.sonatype.flexmojos.common.FlexExtension.RB_SWC;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.jvnet.animal_sniffer.IgnoreJRERequirement;
import org.sonatype.flexmojos.common.FlexDependencySorter;
import org.sonatype.flexmojos.compatibilitykit.FlexCompatibility;
import org.sonatype.flexmojos.test.util.PathUtil;
import org.sonatype.flexmojos.utilities.MavenUtils;

import flex2.tools.oem.Configuration;
import flex2.tools.oem.Library;
import flex2.tools.oem.internal.OEMConfiguration;

/**
 * <p>
 * Goal which compiles the Flex sources into a library for either Flex or AIR depending.
 * </p>
 * <p>
 * The Flex Compiler plugin compiles all ActionScript sources. It can compile the source into 'swc' files. The plugin
 * supports the 'swc' packaging.
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 1.0
 * @goal compile-swc
 * @requiresDependencyResolution compile
 * @phase compile
 */
public class LibraryMojo
    extends AbstractFlexCompilerMojo<Library, FlexDependencySorter>
{

    /**
     * Enable or disable the computation of a digest for the created swf library. This is equivalent to using the
     * <code>compiler.computDigest</code> in the compc compiler.
     * 
     * @parameter default-value="true"
     */
    private boolean computeDigest;

    /**
     * This is the equilvalent of the <code>include-classes</code> option of the compc compiler.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;includeClasses&gt;
     *   &lt;class&gt;AClass&lt;/class&gt;
     *   &lt;class&gt;BClass&lt;/class&gt;
     * &lt;/includeClasses&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] includeClasses;

    /**
     * This is equilvalent to the <code>include-file</code> option of the compc compiler.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;includeFiles&gt;
     *   &lt;file&gt;${baseDir}/anyFile.txt&lt;/file&gt;
     * &lt;/includeFiles&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] includeFiles;

    /**
     * This is equilvalent to the <code>include-namespaces</code> option of the compc compiler.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;includeNamespaces&gt;
     *   &lt;namespace&gt;http://www.adobe.com/2006/mxml&lt;/namespace&gt;
     * &lt;/includeNamespaces&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] includeNamespaces;

    /**
     * This is the equilvalent of the <code>include-sources</code> option of the compc compiler.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;includeSources&gt;
     *   &lt;sources&gt;${baseDir}/src/main/flex&lt;/sources&gt;
     * &lt;/includeSources&gt;
     * </pre>
     * 
     * @parameter
     */
    protected File[] includeSources;

    /**
     * if true, manifest entries with lookupOnly=true are included in SWC catalog. default is false. This exists only so
     * that manifests can mention classes that come in from filespec rather than classpath, e.g. in playerglobal.swc.
     * 
     * @parameter default-value="false"
     */
    private boolean includeLookupOnly;

    /**
     * Sets the RSL output directory.
     * 
     * @parameter
     */
    private File directory;

    /**
     * Adds a CSS stylesheet to this <code>Library</code> object. This is equilvalent to the
     * <code>include-stylesheet</code> option of the compc compiler.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;includeStylesheet&gt;
     *   &lt;stylesheet&gt;
     *     &lt;name&gt;style1&lt;/name&gt;
     *     &lt;path&gt;${baseDir}/src/main/flex/style1.css&lt;/path&gt;
     *   &lt;/stylesheet&gt;
     * &lt;/includeStylesheet&gt;
     * </pre>
     * 
     * @parameter
     */
    private Stylesheet[] includeStylesheet;

    /**
     * Turn on generation of debuggable SWFs. False by default for mxmlc, but true by default for compc.
     * 
     * @parameter default-value="true"
     */
    private boolean debug;

    /**
     * By default, Maven generated archives include the META-INF/maven directory, which contains the pom.xml file used
     * to build the archive. <BR>
     * To disable the generation of these files, include the following configuration for your plugin
     * 
     * @parameter default-value="true"
     */
    private boolean addMavenDescriptor;

    /**
     * workaround for flex compiler configuration dump
     */
    private List<String> includeFilesNames;

    private List<String> includeFilesPaths;

    @Override
    protected void fixConfigReport( FlexConfigBuilder configBuilder )
    {
        super.fixConfigReport( configBuilder );

        if ( !checkNullOrEmpty( includeNamespaces ) )
        {
            configBuilder.addList( includeNamespaces, "include-namespaces", "uri" );
        }
        if ( !checkNullOrEmpty( includeClasses ) )
        {
            configBuilder.addList( includeClasses, "include-classes", "class" );
        }
        if ( !checkNullOrEmpty( includeSources ) )
        {
            configBuilder.addList( includeSources, "include-sources", "path-element" );
        }
        if ( !checkNullOrEmpty( includeFiles ) )
        {
            configBuilder.addIncludeFiles( includeFilesNames, includeFilesPaths );
        }
    }

    @Override
    public void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        // need to initialize builder before go super
        builder = new Library();
        dependencySorter = new FlexDependencySorter();
        dependencySorter.sort( project );

        if ( directory != null )
        {
            builder.setDirectory( directory );
        }

        super.setUp();

        builder.setOutput( getOutput() );

        if ( checkNullOrEmpty( includeClasses ) && checkNullOrEmpty( includeFiles )
            && checkNullOrEmpty( includeNamespaces ) && checkNullOrEmpty( includeSources )
            && checkNullOrEmpty( includeStylesheet ) && checkNullOrEmpty( includeResourceBundles ) )
        {
            getLog().warn( "Nothing expecified to include. Assuming source and resources folders." );
            List<File> sourcePaths = new ArrayList<File>( Arrays.asList( this.sourcePaths ) );
            sourcePaths.remove( new File( resourceBundlePath ) );
            includeSources = sourcePaths.toArray( new File[0] );
            includeFiles = listAllResources();
        }

        if ( !checkNullOrEmpty( includeClasses ) )
        {
            for ( String asClass : includeClasses )
            {
                builder.addComponent( asClass );
            }
        }

        if ( !checkNullOrEmpty( includeFiles ) )
        {
            if ( configurationReport )
            {
                includeFilesNames = new ArrayList<String>();
                includeFilesPaths = new ArrayList<String>();
            }

            for ( String includeFile : includeFiles )
            {
                if ( includeFile == null )
                {
                    throw new MojoFailureException( "Cannot include a null file" );
                }

                File file = MavenUtils.resolveResourceFile( project, includeFile );

                File folder = getResourceFolder( file );

                // If the resource is external to project add on root
                String relativePath = PathUtil.getRelativePath( folder, file );
                if ( relativePath.startsWith( ".." ) )
                {
                    relativePath = file.getName();
                }
                relativePath = relativePath.replace( '\\', '/' );

                if ( configurationReport )
                {
                    includeFilesNames.add( relativePath );
                    includeFilesPaths.add( file.getAbsolutePath() );
                }

                builder.addArchiveFile( relativePath, file );
            }
        }

        if ( !checkNullOrEmpty( includeNamespaces ) )
        {
            for ( String uri : includeNamespaces )
            {
                try
                {
                    builder.addComponent( new URI( uri ) );
                }
                catch ( URISyntaxException e )
                {
                    throw new MojoExecutionException( "Invalid URI " + uri, e );
                }
            }
        }

        if ( !checkNullOrEmpty( includeSources ) )
        {
            for ( File file : includeSources )
            {
                if ( file == null )
                {
                    throw new MojoFailureException( "Cannot include a null file" );
                }
                if ( !file.getName().contains( "{locale}" ) && !file.exists() )
                {
                    throw new MojoFailureException( "File " + file + " not found" );
                }
                builder.addComponent( file );
            }
        }

        includeStylesheet();

        computeDigest();

        if ( addMavenDescriptor )
        {
            builder.addArchiveFile( "maven/" + project.getGroupId() + "/" + project.getArtifactId() + "/pom.xml",
                                    new File( project.getBasedir(), "pom.xml" ) );
        }
    }

	@Override
    protected void configure()
        throws MojoFailureException, MojoExecutionException
    {
        super.configure();

        // workaround for Adobe bug, themes applicable only for Application, but compiler is stupid
        // ( defaults:[-1,-1] unable to open './themes/Spark/spark.css')
        // If you need reference to theme's classes for some reasons, you must use theme's SWC as external dependency.
        // Official document http://livedocs.adobe.com/flex/3/build_deploy_flex3.pdf doesn't contains theme option in compc option
        configuration.setTheme( new File[0] );
    }


    @Override
    protected void configureViaCommandLine( List<String> commandLineArguments )
    {
        super.configureViaCommandLine( commandLineArguments );

        if ( includeLookupOnly )
        {
            commandLineArguments.add( "-include-lookup-only" );
        }
    }

    protected void configureIncludeResourceBundles( OEMConfiguration oemConfig )
    {
        if ( includeResourceBundles != null )
        {
            for ( String bundle : includeResourceBundles )
            {
                builder.addResourceBundle( bundle );
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    private String[] listAllResources()
    {
        List<String> resources = new ArrayList<String>();

        List<Resource> resourcesDirs = project.getResources();
        for ( Resource resource : resourcesDirs )
        {
            File resourceDir = new File( resource.getDirectory() );
            if ( !resourceDir.exists() )
            {
                continue;
            }

            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( resourceDir );
            scanner.addDefaultExcludes();
            scanner.scan();
            String[] files = scanner.getIncludedFiles();
            resources.addAll( Arrays.asList( files ) );
        }

        return resources.toArray( new String[resources.size()] );
    }

    @FlexCompatibility( minVersion = "3" )
    @IgnoreJRERequirement
    private void computeDigest()
    {
        configuration.enableDigestComputation( computeDigest );
    }

    @FlexCompatibility( minVersion = "3" )
    @IgnoreJRERequirement
    private void includeStylesheet()
        throws MojoExecutionException
    {
        if ( !checkNullOrEmpty( includeStylesheet ) )
        {
            for ( Stylesheet sheet : includeStylesheet )
            {
                if ( !sheet.getPath().exists() )
                {
                    throw new MojoExecutionException( "Stylesheet not found: " + sheet.getPath() );
                }
                builder.addStyleSheet( sheet.getName(), sheet.getPath() );
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    private File getResourceFolder( File file )
    {
        String absolutePath = file.getAbsolutePath();
        for ( File sourcePath : sourcePaths )
        {
            if ( absolutePath.startsWith( sourcePath.getAbsolutePath() ) )
            {
                return sourcePath;
            }
        }
        List<String> sourceRoots;
        if ( project.getExecutionProject() != null )
        {
            sourceRoots = project.getExecutionProject().getCompileSourceRoots();
        }
        else
        {
            sourceRoots = project.getCompileSourceRoots();
        }
        for ( String sourcePath : sourceRoots )
        {
            if ( absolutePath.startsWith( sourcePath ) )
            {
                return new File( sourcePath );
            }
        }

        for ( Resource resource : (List<Resource>) project.getResources() )
        {
            if ( absolutePath.startsWith( resource.getDirectory() ) )
            {
                return new File( resource.getDirectory() );
            }
        }
        return project.getBasedir();
    }

    private boolean checkNullOrEmpty( Object[] array )
    {
        if ( array == null )
        {
            return true;
        }

        if ( array.length == 0 )
        {
            return false;
        }

        return false;
    }

    @Override
    protected void writeResourceBundle( String[] bundles, String locale, File localePath )
        throws MojoExecutionException
    {
        Library localized = new Library();
        localized.setConfiguration( getResourceBundleConfiguration( locale, localePath ) );
        localized.setLogger( new CompileLogger( getLog() ) );

        for ( String bundle : bundles )
        {
            localized.addResourceBundle( bundle );
        }

        File output = getRuntimeLocaleOutputFile( locale, RB_SWC );

        localized.setOutput( output );

        build( localized, true );

        if ( configurationReport )
        {
            try
            {
                FlexConfigBuilder configBuilder = new FlexConfigBuilder( localized );
                configBuilder.addOutput( output );
                configBuilder.addList( bundles, "include-resource-bundles", "bundle" );
                configBuilder.write( new File( output.getPath().replace( "." + RB_SWC, "-config-report.xml" ) ) );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "An error has ocurried while recording config-report", e );
            }
        }

        projectHelper.attachArtifact( project, RB_SWC, locale, output );
    }

    @Override
    protected boolean isDebug()
    {
        return this.debug;
    }

    @Override
    protected boolean isApplication()
    {
        return false;
    }

    @Override
    protected String getDefaultLocale()
    {
        throw new UnsupportedOperationException( "Default locale is not available to Libraries" );
    }

    protected Configuration getResourceBundleConfiguration( String locale, File localePath )
        throws MojoExecutionException
    {
        configuration.setLibraryPath( new File[0] );

        configuration.addLibraryPath( new File[] { getOutput() } );
        setLocales( locale );

        if ( localePath != null )
        {
            configuration.setSourcePath( new File[] { localePath } );
        }

        configuration.addLibraryPath( getResourcesBundles( locale ) );

        return configuration;
    }

}
