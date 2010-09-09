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
package org.sonatype.flexmojos.plugin.compiler.attributes.converter;

public class Module
{

    private String destinationPath;

    private String finalName;

    private boolean optimize = true;

    private String sourceFile;

    public String getDestinationPath()
    {
        return destinationPath;
    }

    public String getFinalName()
    {
        return finalName;
    }

    public String getSourceFile()
    {
        return sourceFile;
    }

    public boolean isOptimize()
    {
        return optimize;
    }

    public void setDestinationPath( String destinationPath )
    {
        this.destinationPath = destinationPath;
    }

    public void setFinalName( String finalName )
    {
        this.finalName = finalName;
    }

    public void setOptimize( boolean optimize )
    {
        this.optimize = optimize;
    }

    public void setSourceFile( String sourceFile )
    {
        this.sourceFile = sourceFile;
    }

}
