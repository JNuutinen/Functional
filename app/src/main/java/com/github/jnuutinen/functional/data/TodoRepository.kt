package com.github.jnuutinen.functional.data

import com.github.jnuutinen.functional.data.db.dao.TodoDao
import com.github.jnuutinen.functional.data.db.dao.TodoListDao
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoList
import com.github.jnuutinen.functional.util.runOnIoThread

class TodoRepository private constructor(
    private val mTodoDao: TodoDao,
    private val mTodoListDao: TodoListDao
) {
    fun getListsWithTodos() = mTodoListDao.getAllListsWithTodos()

    fun deleteTodo(todo: Todo) {
        runOnIoThread { mTodoDao.delete(todo) }
    }

    fun deleteTodoList(listId: Int) {
        runOnIoThread { mTodoListDao.delete(listId) }
    }

    fun insertTodo(todo: Todo) {
        runOnIoThread { mTodoDao.insert(todo) }
    }

    fun insertTodoList(todoList: TodoList) {
        runOnIoThread { mTodoListDao.insert(todoList) }
    }

    fun onTodoDelete(positionsToUpdate: List<Todo>, deletedTodo: Todo) {
        runOnIoThread {
            mTodoDao.updateAll(positionsToUpdate)
            mTodoDao.delete(deletedTodo)
        }
    }

    fun onTodoDeleteUndo(positionsToUpdate: List<Todo>, insertedTodo: Todo) {
        runOnIoThread {
            mTodoDao.updateAll(positionsToUpdate)
            mTodoDao.insert(insertedTodo)
        }
    }

    fun updateTodoList(listId: Int, updatedName: String) {
        runOnIoThread { mTodoListDao.update(listId, updatedName) }
    }

    fun updateTodos(todos: List<Todo>) {
        runOnIoThread { mTodoDao.updateAll(todos) }
    }

    companion object {
        @Suppress("unused")
        private val TAG by lazy { TodoRepository::class.java.simpleName }
        @Volatile private var mInstance: TodoRepository? = null

        fun getInstance(todoDao: TodoDao, todoListDao: TodoListDao) =
            mInstance ?: synchronized(this) {
                mInstance ?: TodoRepository(todoDao, todoListDao).also { mInstance = it }
            }
    }
}