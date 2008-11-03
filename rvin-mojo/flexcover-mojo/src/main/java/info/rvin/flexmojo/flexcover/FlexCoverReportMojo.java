package info.rvin.flexmojo.flexcover;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import info.rvin.flexmojos.utilities.MavenUtils;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;

/**
 * Instruments, Tests, and Generates a FlexCover Report.
 * 
 * @goal flexcover
 * @execute phase="test" lifecycle="flexcover"
 */
public class FlexCoverReportMojo
    extends AbstractMavenReport
{
    /**
     * The format of the report. (supports 'html' or 'xml'. defaults to 'html')
     * 
     * @parameter expression="${flexcover.report.format}"
     * @deprecated
     */
    @Deprecated
    private String format;

    /**
     * The format of the report. (can be 'html' and/or 'xml'. defaults to 'html')
     * 
     * @parameter
     */
    private String[] formats = new String[] { "html" };

    /**
     * Maximum memory to pass to JVM of FlexCover processes.
     * 
     * @parameter expression="${flexcover.maxmem}"
     */
    private String maxmem = "64m";

    /**
     * <p>
     * The Datafile Location.
     * </p>
     * 
     * @parameter expression="${flexcover.datafile}" default-value="${project.build.directory}/flexcover/touch.txt"
     * @required
     * @readonly
     */
    protected File dataFile;

    /**
     * <i>Maven Internal</i>: List of artifacts for the plugin.
     * 
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    protected List pluginClasspathList;

    /**
     * The output directory for the report.
     * 
     * @parameter default-value="${project.reporting.outputDirectory}/flexcover"
     * @required
     */
    private File outputDirectory;

    /**
     * Only output flexcover errors, avoid info messages.
     * 
     * @parameter expression="${quiet}" default-value="false"
     * @since 2.1
     */
    private boolean quiet;

    /**
     * <i>Maven Internal</i>: The Doxia Site Renderer.
     * 
     * @component
     */
    private Renderer siteRenderer;

    /**
     * <i>Maven Internal</i>: Project to interact with.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName( Locale locale )
    {
        return "FlexCover Test Coverage";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription( Locale locale )
    {
        return "FlexCover Test Coverage Report.";
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    @Override
    protected String getOutputDirectory()
    {
        return outputDirectory.getAbsolutePath();
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    @Override
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    @Override
    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#generate(org.codehaus.doxia.sink.Sink, java.util.Locale)
     */
    @Override
    public void generate( Sink sink, Locale locale )
        throws MavenReportException
    {
        getLog().info(
                       "Flex-mojos " + MavenUtils.getFlexMojosVersion()
                           + " - GNU GPL License (NO WARRANTY) - See COPYRIGHT file" );

        executeReport( locale );
    }

    // private void executeReportTask( Task task, String format )
    // throws MavenReportException
    // {
    // task.setOutputFormat( format );
    //
    // // execute task
    // try
    // {
    // task.execute();
    // }
    // catch ( MojoExecutionException e )
    // {
    // // throw new MavenReportException( "Error in FlexCover Report generation: " + e.getMessage(), e );
    // // better don't break the build if report is not generated, also due to the sporadic MCOBERTURA-56
    // getLog().error( "Error in FlexCover Report generation: " + e.getMessage(), e );
    // }
    // }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
     */
    @Override
    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        if ( !canGenerateReport() )
        {
            return;
        }

        // ReportTask task = new ReportTask();
        //
        // // task defaults
        // task.setLog( getLog() );
        // task.setPluginClasspathList( pluginClasspathList );
        // task.setQuiet( quiet );
        //
        // // task specifics
        // task.setMaxmem( maxmem );
        // task.setDataFile( dataFile );
        // task.setOutputDirectory( outputDirectory );
        // task.setCompileSourceRoots( getCompileSourceRoots() );
        //
        // if ( format != null )
        // {
        // formats = new String[] { format };
        // }
        //
        // for ( int i = 0; i < formats.length; i++ )
        // {
        // executeReportTask( task, formats[i] );
        // }
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName()
    {
        return "flexcover/index";
    }

    @Override
    public boolean isExternalReport()
    {
        return true;
    }

    @Override
    public boolean canGenerateReport()
    {
        /*
         * Don't have to check for source directories or java code or the like for report generation. Checks for source
         * directories or java project classpath existance should only occur in the Instrument Mojo.
         */
        if ( dataFile == null || !dataFile.exists() )
        {
            getLog().info(
                           "Not executing flexcover:report as the flexcover data file (" + dataFile
                               + ") could not be found" );
            return false;
        }
        else
        {
            return true;
        }
    }

    private List getCompileSourceRoots()
    {
        return project.getExecutionProject().getCompileSourceRoots();
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#setReportOutputDirectory(java.io.File)
     */
    @Override
    public void setReportOutputDirectory( File reportOutputDirectory )
    {
        if ( ( reportOutputDirectory != null ) && ( !reportOutputDirectory.getAbsolutePath().endsWith( "flexcover" ) ) )
        {
            this.outputDirectory = new File( reportOutputDirectory, "flexcover" );
        }
        else
        {
            this.outputDirectory = reportOutputDirectory;
        }
    }
}
