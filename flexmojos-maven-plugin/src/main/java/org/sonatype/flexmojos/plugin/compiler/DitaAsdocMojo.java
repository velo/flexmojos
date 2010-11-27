package org.sonatype.flexmojos.plugin.compiler;

import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;

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
import org.sonatype.flexmojos.util.PathUtil;

/**
 * <p>
 * Goal which generates DITA documentation from the ActionScript sources for SWC projects
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.5
 * @goal dita-asdoc
 * @requiresDependencyResolution compile
 * @phase package
 * @configurator flexmojos
 * @threadSafe
 */
public class DitaAsdocMojo
    extends AsdocMojo
{

    /**
     * Skips flexmojos dita-asdoc goal execution
     * 
     * @parameter expression="${flexmojos.ditaSkip}"
     */
    protected boolean ditaSkip;

    /**
     * The output directory for the generated documentation.
     * 
     * @parameter default-value="${project.build.directory}/dita-asdoc"
     */
    protected File ditaOutputDirectory;

    @Override
    public boolean isSkip()
    {
        return super.isSkip() || ditaSkip;
    }

    public String getOutput()
    {
        ditaOutputDirectory.mkdirs();
        return PathUtil.path( ditaOutputDirectory );
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !SWC.equals( project.getPackaging() ) )
        {
            getLog().warn( "Skipping dita asdoc, it only works for SWC projects. " + project.getPackaging() );
            return;
        }

        File output = project.getArtifact().getFile();
        if ( !output.exists() )
        {
            getLog().warn( "Skipping dita asdoc, associated SWC file doesn't exist " + output );
            return;
        }

        if ( !PathUtil.existAny( getSourcePath() ) )
        {
            getLog().warn( "Skipping asdoc, source path doesn't exist." );
            return;
        }

        wait( executeCompiler( this, true ) );

        ZipOutputStream out = null;
        try
        {
            File tmp = new File( getTargetDirectory(), "temp" );
            tmp.mkdirs();

            File temp = File.createTempFile( getFinalName(), SWC, tmp );

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

            File ditaSource = new File( ditaOutputDirectory, "tempdita" );

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
    public final Boolean getKeepXml()
    {
        return true;
    }

    @Override
    public final Boolean getSkipXsl()
    {
        return true;
    }
}
