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
package net.flexmojos.oss.plugin.report;

import java.io.File;
import java.util.Locale;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import net.flexmojos.oss.plugin.SourcePathAware;
import net.flexmojos.oss.plugin.compiler.lazyload.NotCacheable;
import net.flexmojos.oss.util.PathUtil;

@SuppressWarnings( "deprecation" )
public aspect MojoReport
{

    declare parents : ( AsdocReportMojo || CoverageReportMojo ) implements MavenReport, SourcePathAware, Mojo;

    @NotCacheable
    public String MavenReport.getCategoryName()
    {
        return MavenReport.CATEGORY_PROJECT_REPORTS;
    }

    public boolean MavenReport.canGenerateReport()
    {
        return PathUtil.existAny( ( (SourcePathAware) this ).getSourcePath() );
    }

    public void MavenReport.generate( Sink sink, Locale locale )
        throws MavenReportException
    {
        // Not really sure why this class loader switching
        // is necessary. But it is.
        Thread currentThread = Thread.currentThread();
        ClassLoader savedCL = currentThread.getContextClassLoader();
        try
        {
            currentThread.setContextClassLoader( getClass().getClassLoader() );
            ( (Mojo) this ).execute();
        }
        catch ( Exception e )
        {
            throw new MavenReportException( "Unable to generate report", e );
        }
        finally
        {
            currentThread.setContextClassLoader( savedCL );
        }
    }

    public boolean MavenReport.isExternalReport()
    {
        return true;
    }

    public void MavenReport.setReportOutputDirectory( File outputDirectory )
    {
    }

}
