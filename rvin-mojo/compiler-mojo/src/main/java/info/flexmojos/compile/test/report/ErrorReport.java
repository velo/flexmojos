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
package info.flexmojos.compile.test.report;

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
