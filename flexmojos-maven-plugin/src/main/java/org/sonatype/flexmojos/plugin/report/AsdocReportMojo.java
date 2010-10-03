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
import java.util.Locale;

import org.apache.maven.reporting.MavenReport;
import org.sonatype.flexmojos.plugin.compiler.AsdocMojo;
import org.sonatype.flexmojos.util.PathUtil;

/**
 * <p>
 * Goal which generates documentation from the ActionScript sources.
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 4.0
 * @goal asdoc-report
 * @requiresDependencyResolution compile
 * @phase process-sources
 * @configurator flexmojos
 * @threadSafe
 */
public class AsdocReportMojo
    extends AsdocMojo
    implements MavenReport
{

    /**
     * Specifies the destination directory where asdoc saves the generated HTML files.
     * 
     * @parameter expression="${project.reporting.outputDirectory}/asdoc"
     * @readonly
     * @required
     */
    protected File asdocOutputDirectory;

    /**
     * The description of the AsDoc report.
     * 
     * @parameter expression="${flex.description}" default-value="ASDoc API documentation."
     */
    private String description;

    /**
     * The name of the AsDoc report.
     * 
     * @parameter expression="${flex.name}" default-value="ASDocs"
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

    @Override
    public String getOutput()
    {
        return PathUtil.getPath( getReportOutputDirectory() );
    }

    public String getOutputName()
    {
        return "asdoc/index";
    }

    public File getReportOutputDirectory()
    {
        return asdocOutputDirectory;
    }
}
