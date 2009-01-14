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
package org.epseelon.samples.todolist.domain.hibernate;

import org.epseelon.samples.todolist.domain.TodoItem;
import org.epseelon.samples.todolist.domain.TodoItemRepository;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class TodoItemHibernateDao extends HibernateDaoSupport implements TodoItemRepository {
    public TodoItem save(TodoItem todoItem) {
        getHibernateTemplate().saveOrUpdate(todoItem);
        return todoItem;
    }

    public void remove(TodoItem todoItem) {
        getHibernateTemplate().delete(todoItem);
    }

    public TodoItem findById(TodoItem todoItem) throws Exception {
        long id = todoItem.getId();
        todoItem = (TodoItem) getHibernateTemplate().get(TodoItem.class, todoItem.getId());

        if (todoItem == null)
            throw new Exception("Could not find an item with id " + id);
        return todoItem;
    }

    @SuppressWarnings("unchecked")
    public List<TodoItem> getList() {
        return (List<TodoItem>) getHibernateTemplate().loadAll(TodoItem.class);
    }
}
