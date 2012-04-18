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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

@SuppressWarnings( "unused" )
public class TestCoverageReport
{

    private String classname;

    private Integer[] touchs;

    private Xpp3Dom dom;

    public TestCoverageReport( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public String getClassname()
    {
        return dom.getAttribute( "classname" );
    }

    public void setClassname( String classname )
    {
        throw new UnsupportedOperationException();
    }

    public Integer[] getTouchs()
    {
        if ( touchs == null )
        {
            List<Integer> t = new ArrayList<Integer>();
            Xpp3Dom[] children = dom.getChildren("touch");
            for ( Xpp3Dom c : children )
            {
                t.add( Integer.valueOf( c.getValue() ) );
            }

            touchs = t.toArray( new Integer[0] );
        }
        return touchs;
    }

    public void setTouchs( Integer[] touchs )
    {
        throw new UnsupportedOperationException();
    }
}
