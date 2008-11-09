/*
 * Flex-mojos is a set of maven plugins to allow maven users to compile, optimize, test and ... Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008 Marvin Herman Froeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package info.flexmojos.compile.test.report;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "testMethodReport" )
public class TestMethodReport
{

    private ErrorReport error;

    private ErrorReport failure;

    private String name;

    private double time;

    public ErrorReport getError()
    {
        return error;
    }

    public ErrorReport getFailure()
    {
        return failure;
    }

    public String getName()
    {
        return name;
    }

    public double getTime()
    {
        return time;
    }

    public void setError( ErrorReport error )
    {
        this.error = error;
    }

    public void setFailure( ErrorReport failure )
    {
        this.failure = failure;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setTime( double time )
    {
        this.time = time;
    }

}
