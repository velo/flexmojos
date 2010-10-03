package org.sonatype.flexmojos.plugin.report;

import java.io.File;
import java.util.Locale;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.sonatype.flexmojos.plugin.SourcePathAware;
import org.sonatype.flexmojos.plugin.compiler.lazyload.NotCacheable;
import org.sonatype.flexmojos.util.PathUtil;

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
