package org.sonatype.flexmojos.asdoc;

import static org.sonatype.flexmojos.common.FlexExtension.SWC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Goal which generates documentation from the ActionScript sources in DITA format.
 * 
 * @phase package
 * @goal attach-dita-asdoc
 * @requiresDependencyResolution
 */
public class AttachDitaAsdocMojo
    extends DitaAsdocMojo
{

    /**
     * The filename of the SWF movie to create
     * 
     * @parameter default-value="${project.build.directory}/${project.build.finalName}.swc"
     */
    private File output;

    @Override
    protected void tearDown()
        throws MojoExecutionException, MojoFailureException
    {
        super.tearDown();

        if ( !output.exists() )
        {
            throw new MojoFailureException( "Unable to attach Dita Asdoc.  Swc file doesn't exists! " + output );
        }

        ZipOutputStream out = null;
        try
        {
            File tmp = new File( build.getDirectory(), "temp" );
            tmp.mkdirs();

            File temp = File.createTempFile( build.getFinalName(), SWC, tmp );

            FileUtils.copyFile( output, temp );
            output.delete();

            ZipFile source = new ZipFile( temp );
            out = new ZipOutputStream( new FileOutputStream( output ) );

            Enumeration<? extends ZipEntry> entries = source.entries();
            while ( entries.hasMoreElements() )
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                out.putNextEntry( entry );

                InputStream input = source.getInputStream( entry );
                try
                {
                    IOUtil.copy( input, out );
                }
                finally
                {
                    IOUtil.close( input );
                    out.closeEntry();
                }
            }

            File ditaSource = new File( outputDirectory, "tempdita" );

            DirectoryScanner scan = new DirectoryScanner();
            scan.setBasedir( ditaSource );
            scan.setIncludes( new String[] { "**/*" } );
            scan.addDefaultExcludes();
            scan.scan();

            String[] ditaDocs = scan.getIncludedFiles();
            for ( String doc : ditaDocs )
            {
                out.putNextEntry( new ZipEntry( "docs/" + doc.replace( '\\', '/' ) ) );

                InputStream input = new FileInputStream( new File( ditaSource, doc ) );
                try
                {
                    IOUtil.copy( input, out );
                }
                finally
                {
                    IOUtil.close( input );
                    out.closeEntry();
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( out );
        }
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !SWC.equals( project.getPackaging() ) )
        {
            getLog().warn( "Unable to attach Dita Asdoc, only possible on SWC projects." );
            return;
        }

        super.execute();
    }

}
