package org.sonatype.flexmojos.coverage.emma;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.flexmojos.coverage.AbstractCoverageReporter;
import org.sonatype.flexmojos.coverage.CoverageReportException;
import org.sonatype.flexmojos.coverage.CoverageReportRequest;
import org.sonatype.flexmojos.coverage.CoverageReporter;
import org.sonatype.flexmojos.coverage.util.ApparatUtil;
import org.sonatype.flexmojos.util.PathUtil;

import apparat.tools.coverage.CoverageObserver;

import com.vladium.emma.IAppConstants;
import com.vladium.emma.data.DataFactory;
import com.vladium.emma.data.ICoverageData;
import com.vladium.emma.report.ReportProcessor;
import com.vladium.util.XProperties;

@Component( role = CoverageReporter.class, hint = "emma", instantiationStrategy = "per-lookup" )
public class EmmaCoverageReport
    extends AbstractCoverageReporter
    implements CoverageReporter, Initializable
{

    private ICoverageData cdata;

    public void initialize()
        throws InitializationException
    {
        this.cdata = DataFactory.newCoverageData();
    }

    @Override
    protected CoverageObserver getInstumentationObserver()
    {
        return new CoverageObserver()
        {
            public void instrument( String file, int line )
            {
                String classname = ApparatUtil.toClassname( file );
                synchronized ( cdata.lock() )
                {
                    boolean[][] cover = new boolean[0][0];
                    cdata.addClass( cover, classname, line );
                }
            }
        };
    }

    public void generateReport( CoverageReportRequest request )
        throws CoverageReportException
    {
        File dataDirectory = request.getDataDirectory();
        File reportDirectory = request.getReportDestinationDir();

        File coverageFile = new File( dataDirectory, "coverage.ec" );

        XProperties properties = new XProperties();
        properties.setProperty( "report.html.out.file", PathUtil.getPath( new File( reportDirectory, "index.html" ) ) );
        properties.setProperty( "report.xml.out.file", PathUtil.getPath( new File( reportDirectory, "coverage.xml" ) ) );
        properties.setProperty( "report.txt.out.file", PathUtil.getPath( new File( reportDirectory, "coverage.txt" ) ) );
        properties.setProperty( "report.sort", "+name,+block,+method,+class" );
        properties.setProperty( "report.out.encoding", "UTF-8" );
        properties.setProperty( "report.xml.out.encoding", "UTF-8" );
        properties.setProperty( "report.html.out.encoding", "UTF-8" );

        ReportProcessor reporter = ReportProcessor.create();
        reporter.setAppName( IAppConstants.APP_NAME );
        reporter.setDataPath( PathUtil.getPaths( coverageFile ) );
        reporter.setSourcePath( PathUtil.getPaths( request.getSourcePaths() ) );
        try
        {
            reporter.setReportTypes( request.getFormats().toArray( new String[0] ) );
        }
        catch ( RuntimeException e )
        {
            throw new CoverageReportException( "Unsupported report format: " + request.getFormats(), e );
        }
        reporter.setPropertyOverrides( properties );

        reporter.run();
    }

    public void addResult( String classname, Integer[] touchs )
    {
        // mdata.ClassData classData =
        // this.coverageProjectData.getOrCreateClassData( ApparatUtil.toClassname( classname ) );
        // for ( Integer touch : touchs )
        // {
        // classData.touch( touch );
        // }
    }

}
