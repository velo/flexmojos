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
public class TestMethodReport
{

    private ErrorReport error;

    private ErrorReport failure;

    private String name;

    private double time;

    private Xpp3Dom dom;

    public TestMethodReport( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public ErrorReport getError()
    {
        Xpp3Dom child = this.dom.getChild( "error" );
        if ( child != null )
        {
            return new ErrorReport( child );
        }
        return null;
    }

    public ErrorReport getFailure()
    {
        Xpp3Dom child = this.dom.getChild( "failure" );
        if ( child != null )
        {
            return new ErrorReport( child );
        }
        return null;
    }

    public String getName()
    {
        return dom.getAttribute( "name" );
    }

    public double getTime()
    {
        return Double.parseDouble( dom.getAttribute( "time" ) );
    }

    public void setError( ErrorReport error )
    {
        throw new UnsupportedOperationException();
    }

    public void setFailure( ErrorReport failure )
    {
        throw new UnsupportedOperationException();
    }

    public void setName( String name )
    {
        throw new UnsupportedOperationException();
    }

    public void setTime( double time )
    {
        throw new UnsupportedOperationException();
    }

}
