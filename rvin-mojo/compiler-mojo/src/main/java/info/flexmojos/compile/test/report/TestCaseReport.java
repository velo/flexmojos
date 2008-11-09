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

@XStreamAlias( "testCaseReport" )
public class TestCaseReport
{

    private int errors;

    private int failures;

    private TestMethodReport[] methods;

    private String name;

    private int tests;

    private double time;

    public int getErrors()
    {
        return errors;
    }

    public int getFailures()
    {
        return failures;
    }

    public TestMethodReport[] getMethods()
    {
        return methods;
    }

    public String getName()
    {
        return name;
    }

    public int getTests()
    {
        return tests;
    }

    public double getTime()
    {
        return time;
    }

    public void setErrors( int errors )
    {
        this.errors = errors;
    }

    public void setFailures( int failures )
    {
        this.failures = failures;
    }

    public void setMethods( TestMethodReport[] methods )
    {
        this.methods = methods;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public void setTests( int tests )
    {
        this.tests = tests;
    }

    public void setTime( double time )
    {
        this.time = time;
    }

}
