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
package info.flexmojos.test.report;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias( "testcase" )
public class TestMethodReport
{

    private ErrorReport error;

    private ErrorReport failure;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
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
