/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.asdoc;


import java.io.File;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.siterenderer.sink.SiteRendererSink;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.sonatype.flexmojos.utilities.MavenUtils;

/**
 * Generates documentation for the <code>ActionScript code</code> in the project using the standard Asdoc tool
 * 
 * @author <a href="mailto:justin.edelson@mtvstaff.com">Justin Edelson</a>
 * @goal asdoc-report
 */
public class AsDocReport
    extends AsDocMojo
    implements MavenReport
{

    /**
     * Generates the site report
     * 
     * @component
     */
    private Renderer siteRenderer;

    /**
     * The name of the destination directory.
     * 
     * @parameter expression="${destDir}" default-value="asdoc"
     */
    private String destDir;

    /**
     * The name of the AsDoc report.
     * 
     * @parameter expression="${name}" default-value="ASDocs"
     */
    private String name;

    /**
     * The description of the AsDoc report.
     * 
     * @parameter expression="${description}" default-value="ASDoc API documentation."
     */
    private String description;

    /**
     * Specifies the destination directory where javadoc saves the generated HTML files.
     * 
     * @parameter expression="${project.reporting.outputDirectory}/asdoc"
     * @required
     */
    protected File reportOutputDirectory;

    public boolean canGenerateReport()
    {
        return true;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#execute()
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info(
                       "flexmojos " + MavenUtils.getFlexMojosVersion()
                           + " - GNU GPL License (NO WARRANTY) - See COPYRIGHT file" );
        
        try
        {
            SiteRendererSink sink = siteRenderer.createSink( getReportOutputDirectory(), getOutputName() + ".html" );

            generate( sink, Locale.getDefault() );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "An error has occurred in " + getName( Locale.ENGLISH )
                + " report generation:" + e.getMessage(), e );
        }
    }

    public void generate( Sink sink, Locale locale )
        throws MavenReportException
    {
        String packaging = project.getPackaging();
        if ( !"swc".equals( packaging ) && !"swf".equals( packaging ) )
        {
            getLog().warn( "Invalid packaging for asdoc generation " + packaging );
            return;
        }

        output = getReportOutputDirectory();
        try
        {
            // Not really sure why this class loader switching
            // is necessary. But it is.
            Thread currentThread = Thread.currentThread();
            ClassLoader savedCL = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader( getClass().getClassLoader() );
            super.execute();
            currentThread.setContextClassLoader( savedCL );
        }
        catch ( Exception e )
        {
            throw new MavenReportException( "Unable to generate report", e );
        }

    }

    public String getCategoryName()
    {
        return CATEGORY_PROJECT_REPORTS;
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
        return destDir + "/index";
    }

    public File getReportOutputDirectory()
    {
        if ( reportOutputDirectory == null )
        {
            return output;
        }

        return reportOutputDirectory;
    }

    public boolean isExternalReport()
    {
        return true;
    }

    public void setReportOutputDirectory( File outputDirectory )
    {
        if ( ( outputDirectory != null ) && ( !outputDirectory.getAbsolutePath().endsWith( destDir ) ) )
        {
            this.reportOutputDirectory = new File( outputDirectory, destDir );
        }
        else
        {
            this.reportOutputDirectory = outputDirectory;
        }

    }

}
