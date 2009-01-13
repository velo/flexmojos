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
