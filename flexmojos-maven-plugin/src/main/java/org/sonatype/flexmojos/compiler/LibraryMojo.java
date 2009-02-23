/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.compiler;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.sonatype.flexmojos.compatibilitykit.FlexCompatibility;
import org.sonatype.flexmojos.utilities.MavenUtils;
import org.sonatype.flexmojos.utilities.PathUtil;

import flex2.tools.oem.Library;

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
 * @requiresDependencyResolution
 * @phase compile
 */
public class LibraryMojo
    extends AbstractFlexCompilerMojo<Library>
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
     * Sets the RSL output directory.
     * 
     * @parameter
     */
    private File directory;

    /*
     * TODO how to set this on flex-compiler-oem -include-lookup-only private boolean includeLookupOnly;
     */

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
     * The filename of the SWF movie to create
     * 
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.swc"
     */
    private File output;

    @Override
    public void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        // need to initialize builder before go super
        builder = new Library();

        if ( directory != null )
        {
            builder.setDirectory( directory );
        }

        super.setUp();

        builder.setOutput( getOutput() );

        if ( checkNullOrEmpty( includeClasses ) && checkNullOrEmpty( includeFiles )
            && checkNullOrEmpty( includeNamespaces ) && checkNullOrEmpty( includeSources )
            && checkNullOrEmpty( includeStylesheet ) )
        {
            getLog().warn( "Nothing expecified to include.  Assuming source and resources folders." );
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

                builder.addArchiveFile( relativePath.replace( '\\', '/' ), file );
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
                    throw new MojoFailureException( "File " + file.getName() + " not found" );
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

            Collection<File> files =
                FileUtils.listFiles( resourceDir, HiddenFileFilter.VISIBLE, TrueFileFilter.INSTANCE );
            for ( File file : files )
            {
                resources.add( file.getAbsolutePath() );
            }
        }

        return resources.toArray( new String[resources.size()] );
    }

    @FlexCompatibility( minVersion = "3" )
    private void computeDigest()
    {
        configuration.enableDigestComputation( computeDigest );
    }

    @FlexCompatibility( minVersion = "3" )
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
        localized.setConfiguration( configuration );
        configuration.setLibraryPath( new File[0] );

        localized.setLogger( new CompileLogger( getLog() ) );

        configuration.addLibraryPath( new File[] { getOutput() } );
        setLocales( new String[] { locale } );

        configuration.setSourcePath( new File[] { localePath } );
        for ( String bundle : bundles )
        {
            localized.addResourceBundle( bundle );
        }
        configuration.addLibraryPath( getResourcesBundles( locale ) );

        File output = getRuntimeLocaleOutputFile( locale, RB_SWC );

        localized.setOutput( output );

        build( localized );

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

    @Override
    protected File getOutput()
    {
        return this.output;
    }

}
