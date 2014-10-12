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
package net.flexmojos.oss.plugin.source;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import net.flexmojos.oss.plugin.utilities.SourceFileResolver;

import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

/**
 * Generate the "Source view" documentation from the sources, like Flex/Flash Builder does for the release builds. Users
 * can they right click the application and view the sources.
 * <p>
 * This goal produces a syntax highlighted version of the as, mxml and html documents in the sources, and just copies
 * other types of files. It also generates a navigation to browse the sources.
 * </p>
 * 
 * @goal source-view
 * @phase prepare-package
 */
public class SourceViewMojo
    extends AbstractMojo
{
    /**
     * The Maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The name of the directory containing the "View source" documentation.
     * <p>
     * It must be the same as the one declared in the Flex application:
     * 
     * <pre>
     * &lt;mx:Application [...] viewSourceURL="srcview"&gt;
     *    [...]
     * &lt;/mx:Application&gt;
     * </pre>
     * 
     * </p>
     * 
     * @parameter default-value="srcview"
     */
    protected String sourceViewDirectoryName;

    /**
     * Encoding to use for the generated documentation.
     * 
     * @parameter default-value="UTF-8"
     */
    protected String outputEncoding;

    /**
     * The instance of {@link VelocityEngine}.
     */
    protected VelocityEngine velocityEngine;

    /**
     * The instance of {@link VelocityContext}.
     */
    protected VelocityContext velocityContext;

    /**
     * The file filter to use when processing source files.
     */
    protected IOFileFilter filter = FileFilterUtils.makeCVSAware( FileFilterUtils.makeSVNAware( null ) );

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // Initialize a Velocity engine
        if ( velocityEngine == null )
        {
            velocityEngine = new VelocityEngine();
            try
            {
                Properties p = new Properties();
                p.setProperty( VelocityEngine.RESOURCE_LOADER, "classpath" );
                p.setProperty( "classpath." + VelocityEngine.RESOURCE_LOADER + ".class",
                               ClasspathResourceLoader.class.getName() );
                p.setProperty( VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName() );
                p.setProperty( VelocityEngine.VM_CONTEXT_LOCALSCOPE, Boolean.toString( true ) );
                velocityEngine.init( p );
            }
            catch ( Exception e )
            {
                throw new MojoFailureException( "Failed to initialize Velocity engine", e );
            }
        }

        // Initialize a Velocity context
        velocityContext = new VelocityContext();

        // Create the "Source view" directory
        File sourceViewDirectory =
            new File( project.getBuild().getDirectory() + File.separator + sourceViewDirectoryName );
        sourceViewDirectory.mkdirs();

        // Start processing the main source directory
        processDirectory( new File( project.getBuild().getSourceDirectory() ), sourceViewDirectory );

        // Initialize contents of the Velocity context
        velocityContext.put( "sourceViewDirectory", sourceViewDirectory );
        velocityContext.put( "project", project );
        velocityContext.put( "contentFrameSource", getContentFrameSource() );

        // Generate the HTML pages from the templates
        processTemplate( "index.html", sourceViewDirectory );
        processTemplate( "navigation.html", sourceViewDirectory );
        processTemplate( "style.css", sourceViewDirectory );
    }

    /**
     * Loop through source files in the directory and syntax highlight and/or copy them to the target directory.
     * 
     * @param directory The source directory to process.
     * @param targetDirectory The directory where to store the output.
     */
    protected void processDirectory( File directory, File targetDirectory )
    {
        getLog().debug( "Processing directory " + directory.getName() );

        // Loop through files in the directory
        for ( File file : directory.listFiles( (FileFilter) filter ) )
        {
            // Skip hidden files
            if ( !file.isHidden() && !file.getName().startsWith( "." ) )
            {
                if ( file.isDirectory() )
                {
                    File newTargetDirectory = new File( targetDirectory, file.getName() );
                    newTargetDirectory.mkdir();
                    processDirectory( file, newTargetDirectory );
                }
                else
                {
                    try
                    {
                        processFile( file, targetDirectory );
                    }
                    catch ( IOException e )
                    {
                        getLog().warn( "Error while processing " + file.getName(), e );
                    }
                }
            }
        }
    }

    /**
     * Syntax highlight and/or copy the source file to the target directory.
     * 
     * @param file The file to process.
     * @param targetDirectory The directory where to store the output.
     * @throws IOException If there was a file read/write exception.
     */
    protected void processFile( File file, File targetDirectory )
        throws IOException
    {
        getLog().debug( "Processing file " + file.getName() );

        // Prepare to copy the file
        String destinationFilePath = targetDirectory.getCanonicalPath() + File.separator + file.getName();

        // Check if the file can be syntax highlighted
        String extension = file.getName().substring( file.getName().lastIndexOf( '.' ) + 1 );
        String highlightFilter = getHighlightFilter( extension );

        if ( highlightFilter != null )
        {
            getLog().debug( "Converting " + file.getName() + "to HTML." );
            destinationFilePath += ".html";
            XhtmlRendererFactory.getRenderer( highlightFilter ).highlight( file.getName(), new FileInputStream( file ),
                                                                           new FileOutputStream( destinationFilePath ),
                                                                           Charset.forName( outputEncoding ).name(),
                                                                           false );
        }
        else
        {
            getLog().debug( "Copying " + file.getName() );
            FileUtils.copyFileToDirectory( file, targetDirectory );
        }
    }

    /**
     * Get the syntax highlighting filter to use for a file extension.
     * 
     * @param fileExtension the file extension to test.
     * @return null if no filter available for this file type.
     * @see {@link XhtmlRendererFactory#getSupportedTypes()}
     */
    protected String getHighlightFilter( String fileExtension )
    {
        // FIXME Using file extensions are less trustable than getting the real
        // filetype...

        if ( fileExtension != null && !"".equals( fileExtension ) )
        {
            if ( "as".equals( fileExtension ) )
            {
                return XhtmlRendererFactory.JAVA;
            }
            else if ( fileExtension.startsWith( "xml" ) || fileExtension.endsWith( "xml" ) )
            {
                return XhtmlRendererFactory.XML;
            }
            else if ( fileExtension.startsWith( "htm" ) || fileExtension.startsWith( "xhtm" ) )
            {
                return XhtmlRendererFactory.XHTML;
            }
        }
        return null;
    }

    /**
     * Merge the given template with the {@link SourceViewMojo#velocityContext} and produce the file in the output
     * documentation.
     * 
     * @param templateName The name of the template to process.
     * @param targetDirectory The directory where to store the output file.
     * @throws MojoFailureException If the template could not be loaded, parsed, or the output file could not be
     *             written.
     */
    protected void processTemplate( String templateName, File targetDirectory )
        throws MojoFailureException
    {
        FileWriterWithEncoding pageWriter = null;
        try
        {
            pageWriter =
                new FileWriterWithEncoding( new File( targetDirectory.getCanonicalPath() + File.separator
                    + templateName ), Charset.forName( outputEncoding ) );
            velocityEngine.mergeTemplate( "/templates/source-view/" + templateName + ".vm",
                                          Charset.forName( "UTF-8" ).name(), velocityContext, pageWriter );
        }
        catch ( ResourceNotFoundException e )
        {
            throw new MojoFailureException( "The template '" + templateName + "' could not be found.", e );
        }
        catch ( ParseErrorException e )
        {
            throw new MojoFailureException( "Failed to parse the template '" + templateName + "' .", e );
        }
        catch ( Exception e )
        {
            throw new MojoFailureException( "Failed to load the template '" + templateName + "' .", e );
        }
        finally
        {
            if(pageWriter != null) {
                try {
                    pageWriter.close();
                } catch (IOException e) {
                    throw new MojoFailureException("Failed to write the template '" + templateName + "' .", e);
                }
            }
        }
    }

    /**
     * Resolve the file to assign as the source of the content frame in the generated documentation.
     * <p>
     * Tries to resolve the main source and defaults to a blank page if not found.
     * </p>
     * 
     * @return The path to the page to use in the generated documentation.
     */
    @SuppressWarnings( "unchecked" )
    protected String getContentFrameSource()
    {
        File mainSourceFile =
            SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), null, project.getGroupId(),
                                                  project.getArtifactId() );

        return ( mainSourceFile == null ) ? "about:blank" : mainSourceFile.getName() + ".html";
    }
}
