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
package net.flexmojos.oss.coverage;

import java.io.File;
import java.util.List;

import scala.actors.threadpool.Arrays;

public class CoverageReportRequest
{

    private File dataDirectory;

    private List<String> formats;

    private String encoding;

    private File reportDirectory;

    private List<File> sourcePath;

    @SuppressWarnings( "unchecked" )
    public CoverageReportRequest( File dataDirectory, List<String> formats, String encoding, File reportDirectory,
                                  File... sourcePath )
    {
        super();
        this.dataDirectory = dataDirectory;
        this.formats = formats;
        this.encoding = encoding;
        this.reportDirectory = reportDirectory;
        this.sourcePath = Arrays.asList( sourcePath );
    }

    public File getDataDirectory()
    {
        return dataDirectory;
    }

    public List<String> getFormats()
    {
        return formats;
    }

    public String getReportEncoding()
    {
        return encoding;
    }

    public File getReportDestinationDir()
    {
        return reportDirectory;
    }

    public List<File> getSourcePaths()
    {
        return sourcePath;
    }

}
