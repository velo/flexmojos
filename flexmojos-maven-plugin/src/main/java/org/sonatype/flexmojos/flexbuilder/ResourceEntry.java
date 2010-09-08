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
package org.sonatype.flexmojos.flexbuilder;

public class ResourceEntry
{
    private String destPath;

    private String sourcePath;

    public ResourceEntry( String destPath, String sourcePath )
    {
        this.destPath = destPath;
        this.sourcePath = sourcePath;
    }

    /**
     * Returns the path to where the resource file will be added to the compiled SWC relative to the SWC root.
     * 
     * @return
     */
    public String getDestPath()
    {
        return destPath;
    }

    /**
     * Sets the path to where the resource file will be added to the compiled SWC relative to the SWC root.
     * 
     * @param path
     */
    public void setDestPath( String path )
    {
        destPath = path;
    }

    /**
     * Returns the absolute path to the resource file on the file system. The flex compiler uses this to resolve the
     * resource file.
     * 
     * @return
     */
    public String getSourcePath()
    {
        return sourcePath;
    }

    /**
     * Sets the absolute path to the resource file on the file system. The flex compiler uses this to resolve the
     * resource file.
     * 
     * @param path
     */
    public void setSourcePath( String path )
    {
        sourcePath = path;
    }
}