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
package org.epseelon.samples.todolist.business;

import org.epseelon.samples.todolist.domain.TodoItem;
import org.epseelon.samples.todolist.domain.TodoItemRepository;

import java.util.List;

public class TodoServiceImpl implements TodoService {
    private TodoItemRepository todoItemRepository;

    public void setTodoItemRepository(TodoItemRepository todoItemRepository) {
        this.todoItemRepository = todoItemRepository;
    }

    public TodoItem save(TodoItem item) throws Exception {
        try {
            this.todoItemRepository.save(item);
            return item;
        } catch (Exception e) {
            throw new Exception("Could not save item because: " + e.getCause());
        }
    }

    public void remove(TodoItem item) throws Exception {
        try {
            this.todoItemRepository.remove(item);
        } catch (Exception e) {
            throw new Exception("Could not delete item because " + e.getMessage());
        }
    }

    public TodoItem findById(TodoItem item) throws Exception {
        try {
            return this.todoItemRepository.findById(item);
        } catch (Exception e) {
            throw new Exception("Could not find item because " + e.getMessage());
        }
    }

    public List<TodoItem> getList() throws Exception {
        try {
            return this.todoItemRepository.getList();
        } catch (Exception e) {
            throw new Exception("Could not list items because " + e.getMessage());
        }
    }
}
