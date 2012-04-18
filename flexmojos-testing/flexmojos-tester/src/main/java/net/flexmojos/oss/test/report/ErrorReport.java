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
package net.flexmojos.oss.test.report;

import org.codehaus.plexus.util.xml.Xpp3Dom;

@SuppressWarnings( "unused" )
public class ErrorReport
{
    private String message;

    private String stackTrace;

    private String type;

    private Xpp3Dom dom;

    public ErrorReport( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public String getMessage()
    {
        return dom.getAttribute( "message" );
    }

    public String getStackTrace()
    {
        return dom.getAttribute( "stackTrace" );
    }

    public String getType()
    {
        return dom.getAttribute( "type" );
    }

    public void setMessage( String message )
    {
        throw new UnsupportedOperationException();
    }

    public void setStackTrace( String stackTrace )
    {
        throw new UnsupportedOperationException();
    }

    public void setType( String type )
    {
        throw new UnsupportedOperationException();
    }

}
