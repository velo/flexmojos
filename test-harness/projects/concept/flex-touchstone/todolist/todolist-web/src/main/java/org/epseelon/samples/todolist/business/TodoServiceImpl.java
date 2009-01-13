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
