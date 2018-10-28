package com.github.jnuutinen.functional.data

import com.github.jnuutinen.functional.data.db.dao.TodoDao
import com.github.jnuutinen.functional.data.db.dao.TodoGroupDao
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoGroup
import com.github.jnuutinen.functional.util.runOnIoThread

class TodoRepository private constructor(
    private val todoDao: TodoDao,
    private val todoGroupDao: TodoGroupDao
) {
    fun getTodos() = todoDao.getTodos()

    fun getTodosInGroup(groupId: Int) = todoDao.getTodosInGroup(groupId)

    fun getTodoGroups() = todoGroupDao.getTodoGroups()

    fun deleteTodo(todo: Todo) {
        runOnIoThread { todoDao.deleteTodo(todo) }
    }

    fun deleteTodoGroup(todoGroup: TodoGroup) {
        runOnIoThread { todoGroupDao.deleteTodoGroup(todoGroup) }
    }

    fun deleteTodoGroup(groupId: Int) {
        runOnIoThread { todoGroupDao.deleteTodoGroup(groupId) }
    }

    fun insertTodo(todo: Todo) {
        runOnIoThread { todoDao.insertTodo(todo) }
    }

    fun insertTodoGroup(todoGroup: TodoGroup) {
        runOnIoThread { todoGroupDao.insertTodoGroup(todoGroup) }
    }

    companion object {
        private val TAG by lazy { TodoRepository::class.java.simpleName }
        @Volatile private var instance: TodoRepository? = null

        fun getInstance(todoDao: TodoDao, todoGroupDao: TodoGroupDao) =
            instance ?: synchronized(this) {
                instance ?: TodoRepository(todoDao, todoGroupDao).also { instance = it }
            }
    }
}