/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.todolist.business;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.flexmojos.todolist.domain.TodoItem;

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
