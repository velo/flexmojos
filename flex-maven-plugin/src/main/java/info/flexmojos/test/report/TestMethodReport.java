/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
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
