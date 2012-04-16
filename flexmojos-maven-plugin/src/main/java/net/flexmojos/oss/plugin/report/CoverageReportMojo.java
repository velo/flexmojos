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

import static net.flexmojos.oss.util.PathUtil.files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import net.flexmojos.oss.plugin.AbstractMavenMojo;
import net.flexmojos.oss.plugin.SourcePathAware;

/**
 * Goal to generate coverage report from unit tests
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal coverage-report
 * @execute lifecycle="coveragecycle" phase="test"
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class CoverageReportMojo
    extends AbstractMavenMojo
    implements SourcePathAware
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

    /**
     * The maven compile source roots. List of path elements that form the roots of ActionScript class
     * 
     * @parameter expression="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List<String> sourcePaths;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // nothing to be done, the lifecycle deal with this report generation
    	
    	File index = new File(coverageReportOutputDirectory.getAbsolutePath() + "/index.bak.html");
    	if(index.exists())
    	{
        	// Fix at site phase
        	// Copy index.bak.html to index.html
        	// In the site phase, index.html is opened with a writer by the site plugin and erase its content. index.bak.html is a backup and replaces index.html.
    		try {
    			FileUtils.copyFile(index, new File(coverageReportOutputDirectory.getAbsolutePath() + "/index.html"));
			} catch (IOException e) {
				getLog().error(e.getMessage());
			}
    	}
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
        return "coverage/index";
    }

    public File getReportOutputDirectory()
    {
        coverageReportOutputDirectory.mkdirs();
        return coverageReportOutputDirectory;
    }

    public File[] getSourcePath()
    {
        return files( sourcePaths );
    }

}
