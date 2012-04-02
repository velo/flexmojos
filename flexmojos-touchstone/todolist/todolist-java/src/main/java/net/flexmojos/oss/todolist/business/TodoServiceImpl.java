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
package net.flexmojos.oss.todolist.business;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.flexmojos.oss.todolist.domain.TodoItem;

public class TodoServiceImpl
    implements TodoService
{
    private static Map<String, TodoItem> items = new HashMap<String, TodoItem>();

    public TodoItem save( TodoItem item )
        throws Exception
    {
        if ( item.getId() == null || item.getId().length() == 0 )
        {
            item.setId( Long.toHexString( System.nanoTime() ) );
        }
        items.put( item.getId(), item );
        return item;
    }

    public void remove( TodoItem item )
        throws Exception
    {
        items.remove( item.getId() );
    }

    public TodoItem findById( TodoItem item )
        throws Exception
    {
        return items.get( item.getId() );
    }

    public Collection<TodoItem> getList()
        throws Exception
    {
        return items.values();
    }
}
