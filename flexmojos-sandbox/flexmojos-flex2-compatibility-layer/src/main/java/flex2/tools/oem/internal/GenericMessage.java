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
package flex2.tools.oem.internal;

import flex2.tools.oem.Message;

class GenericMessage
    implements Message
{
    private String level;

    private String path;

    private String message;

    private int line;

    private int col;

    GenericMessage( Message message )
    {
        this( message.getLevel(), message.getPath(), message.getLine(), message.getColumn(), message.toString() );
    }

    GenericMessage( String level, String path, int line, int col, String message )
    {
        this.level = level;
        this.path = path;
        this.line = line;
        this.col = col;
        this.message = message;
    }

    public int getColumn()
    {
        return this.col;
    }

    public String getLevel()
    {
        return this.level;
    }

    public int getLine()
    {
        return this.line;
    }

    public String getPath()
    {
        return this.path;
    }

    public String toString()
    {
        return this.message;
    }
}