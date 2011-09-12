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

    public void generateReport( CoverageReportRequest request, boolean complexityCalculation )
        throws CoverageReportException
    {
    	if ( complexityCalculation ) {
    		getLogger().info( "Complexity Calculation not supported for Emma reports." );
    	}
    	
        File dataDirectory = request.getDataDirectory();
        File reportDirectory = request.getReportDestinationDir();

        File coverageFile = new File( dataDirectory, "coverage.ec" );

        XProperties properties = new XProperties();
        properties.setProperty( "report.html.out.file", PathUtil.path( new File( reportDirectory, "index.html" ) ) );
        properties.setProperty( "report.xml.out.file", PathUtil.path( new File( reportDirectory, "coverage.xml" ) ) );
        properties.setProperty( "report.txt.out.file", PathUtil.path( new File( reportDirectory, "coverage.txt" ) ) );
        properties.setProperty( "report.sort", "+name,+block,+method,+class" );
        properties.setProperty( "report.out.encoding", "UTF-8" );
        properties.setProperty( "report.xml.out.encoding", "UTF-8" );
        properties.setProperty( "report.html.out.encoding", "UTF-8" );

        ReportProcessor reporter = ReportProcessor.create();
        reporter.setAppName( IAppConstants.APP_NAME );
        reporter.setDataPath( PathUtil.paths( coverageFile ) );
        reporter.setSourcePath( PathUtil.paths( request.getSourcePaths() ) );
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
