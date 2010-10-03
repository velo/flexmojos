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
package org.sonatype.flexmojos.plugin.report;

import java.io.File;
import java.util.Collections;
import java.util.Locale;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.reporting.MavenReport;
import org.sonatype.flexmojos.plugin.test.TestRunMojo;

/**
 * Goal to generate coverage report from unit tests
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal coverage-report
 * @execute lifecycle="coveragecycle" phase="test-compile"
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class CoverageReportMojo
    extends TestRunMojo
    implements MavenReport
{

    /**
     * Specifies the destination directory where asdoc saves the generated HTML files.
     * 
     * @parameter expression="${project.reporting.outputDirectory}/coverage"
     * @readonly
     * @required
     */
    protected File coverageReportOutputDirectory;

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
        return "coverage/index";
    }

    public File getReportOutputDirectory()
    {
        coverageReportOutputDirectory.mkdirs();
        return coverageReportOutputDirectory;
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        super.coverage = true;
        super.coverageReportFormat = Collections.singletonList( "html" );
        super.coverageOutputDirectory = coverageReportOutputDirectory;

        super.execute();
    }
}
