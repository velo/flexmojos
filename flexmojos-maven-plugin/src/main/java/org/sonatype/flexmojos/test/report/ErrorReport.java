/**
 *  Copyright 2008 Marvin Herman Froeder
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.sonatype.flexmojos.test.report;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class ErrorReport
{
    @XStreamAsAttribute
    private String message;

    private String stackTrace;

    @XStreamAsAttribute
    private String type;

    public String getMessage()
    {
        return message;
    }

    public String getStackTrace()
    {
        return stackTrace;
    }

    public String getType()
    {
        return type;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    public void setStackTrace( String stackTrace )
    {
        this.stackTrace = stackTrace;
    }

    public void setType( String type )
    {
        this.type = type;
    }

}
