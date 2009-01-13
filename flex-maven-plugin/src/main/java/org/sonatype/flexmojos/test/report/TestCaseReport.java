/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.test.report;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias( "testsuite" )
public class TestCaseReport
{

    @XStreamAsAttribute
    private int errors;

    @XStreamAsAttribute
    private int failures;

    @XStreamImplicit( itemFieldName = "testcase" )
    private List<TestMethodReport> methods;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private int tests;

    @XStreamAsAttribute
    private double time;

    public int getErrors()
    {
        return errors;
    }

    public int getFailures()
    {
        return failures;
    }

    public List<TestMethodReport> getMethods()
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

    public void setMethods( List<TestMethodReport> methods )
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
