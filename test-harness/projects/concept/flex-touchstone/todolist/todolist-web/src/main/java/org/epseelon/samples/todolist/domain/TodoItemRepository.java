package org.epseelon.samples.todolist.domain;

import java.util.List;

public interface TodoItemRepository {
    void remove(TodoItem todoItem);

    TodoItem save(TodoItem todoItem);

    TodoItem findById(TodoItem todoItem) throws Exception;

    List<TodoItem> getList();
}
