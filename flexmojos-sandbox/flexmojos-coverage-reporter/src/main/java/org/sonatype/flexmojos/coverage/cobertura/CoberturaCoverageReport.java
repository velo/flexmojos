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
package org.sonatype.flexmojos.coverage.cobertura;

import java.io.File;
import java.util.List;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.reporting.xml.SummaryXMLReport;
import net.sourceforge.cobertura.reporting.xml.XMLReport;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Source;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.flexmojos.coverage.AbstractCoverageReporter;
import org.sonatype.flexmojos.coverage.CoverageReportException;
import org.sonatype.flexmojos.coverage.CoverageReportRequest;
import org.sonatype.flexmojos.coverage.CoverageReporter;
import org.sonatype.flexmojos.coverage.util.ApparatUtil;
import org.sonatype.flexmojos.util.PathUtil;

import apparat.tools.coverage.CoverageObserver;

@Component( role = CoverageReporter.class, hint = "cobertura", instantiationStrategy = "per-lookup" )
public class CoberturaCoverageReport
    extends AbstractCoverageReporter
    implements CoverageReporter, Initializable
{

    private ProjectData coverageProjectData;

    public void initialize()
        throws InitializationException
    {
        this.coverageProjectData = new ProjectData();
    }

    @Override
    protected CoverageObserver getInstumentationObserver()
    {
        return new CoverageObserver()
        {
            public void instrument( String file, int line )
            {
                ClassData classData = coverageProjectData.getOrCreateClassData( ApparatUtil.toClassname( file ) );
                classData.addLine( line, null, null );
            }
        };
    }

    public void generateReport( CoverageReportRequest request )
        throws CoverageReportException
    {
        File dataDirectory = request.getDataDirectory();

        FileFinder finder = new FileFinder()
        {
            public Source getSource( String fileName )
            {
                Source source = super.getSource( fileName.replace( ".java", ".as" ) );
                if ( source == null )
                {
                    source = super.getSource( fileName.replace( ".java", ".mxml" ) );
                }
                return source;
            }
        };

        List<File> sp = request.getSourcePaths();
        for ( File dir : sp )
        {
            finder.addSourceDirectory( PathUtil.getPath( dir ) );
        }

        ComplexityCalculator complexity = new ComplexityCalculator( finder );
        try
        {
            File coverageReportDestinationDir = request.getReportDestinationDir();
            coverageReportDestinationDir.mkdirs();

            List<String> format = request.getFormats();
            if ( format.contains( "html" ) )
            {
                String coverageReportEncoding = request.getReportEncoding();
                if ( StringUtils.isEmpty( coverageReportEncoding ) )
                {
                    coverageReportEncoding = "UTF-8";
                }
                new HTMLReport( coverageProjectData, coverageReportDestinationDir, finder, complexity,
                                coverageReportEncoding );
            }
            else if ( format.contains( "xml" ) )
            {
                new XMLReport( coverageProjectData, coverageReportDestinationDir, finder, complexity );
            }
            else if ( format.contains( "summaryXml" ) )
            {
                new SummaryXMLReport( coverageProjectData, coverageReportDestinationDir, finder, complexity );
            }
        }
        catch ( Exception e )
        {
            throw new CoverageReportException( "Unable to write coverage report", e );
        }

        CoverageDataFileHandler.saveCoverageData( coverageProjectData, new File( dataDirectory, "cobertura.ser" ) );
    }

    public void addResult( String classname, Integer[] touchs )
    {
        ClassData classData = this.coverageProjectData.getOrCreateClassData( ApparatUtil.toClassname( classname ) );
        for ( Integer touch : touchs )
        {
            classData.touch( touch, 1 );
        }
    }

}
