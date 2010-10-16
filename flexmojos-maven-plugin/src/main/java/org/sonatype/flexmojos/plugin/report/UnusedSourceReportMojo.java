package org.sonatype.flexmojos.plugin.report;

import static org.sonatype.flexmojos.util.PathUtil.path;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.sonatype.flexmojos.plugin.compiler.lazyload.NotCacheable;
import org.sonatype.flexmojos.plugin.unused.UnusedSourceMojo;

/**
 * This goal checks if all source files are included on this build
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal unused-sources-report
 * @execute lifecycle="unusedcycle" phase="process-classes"
 * @threadSafe
 */
@SuppressWarnings( "deprecation" )
public class UnusedSourceReportMojo
    extends UnusedSourceMojo
    implements MavenReport
{
    /**
     * Specifies the destination directory where asdoc saves the generated HTML files.
     * 
     * @parameter expression="${project.reporting.outputDirectory}/unused"
     * @readonly
     * @required
     */
    protected File unusedSourceReportOutputDirectory;

    /**
     * The description of the AsDoc report.
     * 
     * @parameter expression="${flex.description}" default-value="Flexmojos Test Coverage Report."
     */
    private String description;

    /**
     * The name of the AsDoc report.
     * 
     * @parameter expression="${flex.name}" default-value="Coverage"
     */
    private String name;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().error( "This goal is a report and must be executed as a report" );
    }

    public String getDescription( Locale locale )
    {
        return description;
    }

    public String getName( Locale locale )
    {
        return name;
    }

    public String getOutputName()
    {
        return "unused/index";
    }

    public File getReportOutputDirectory()
    {
        unusedSourceReportOutputDirectory.mkdirs();
        return unusedSourceReportOutputDirectory;
    }

    public void generate( Sink sink, Locale locale )
        throws MavenReportException
    {
        sink.head();

        sink.title();
        sink.text( "Unused Source Files Report" );
        sink.title_();

        sink.head_();

        List<File> files = getUnusedFiles();

        sink.body();
        if ( files.isEmpty() )
        {
            sink.paragraph();
            sink.text( "All source files used!" );
            sink.paragraph_();
        }
        else
        {
            sink.paragraph();
            sink.text( "Found the following unused source files:" );
            sink.paragraph_();

            sink.list();
            for ( File file : files )
            {
                sink.listItem();
                sink.text( path( file ) );
                sink.listItem_();
            }
            sink.list_();
        }
        sink.body_();

        sink.flush();

        sink.close();
    }

    @NotCacheable
    public boolean isExternalReport()
    {
        return false;
    }

}
