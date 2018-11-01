package com.github.jnuutinen.functional.data

import com.github.jnuutinen.functional.data.db.dao.TodoDao
import com.github.jnuutinen.functional.data.db.dao.TodoGroupDao
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoGroup
import com.github.jnuutinen.functional.util.runOnIoThread

class TodoRepository private constructor(
    private val mTodoDao: TodoDao,
    private val mTodoGroupDao: TodoGroupDao
) {
    fun getGroupsWithTodos() = mTodoGroupDao.getGroupsWithTodos()

    fun deleteTodo(todo: Todo) {
        runOnIoThread { mTodoDao.deleteTodo(todo) }
    }

    fun deleteTodoGroup(groupId: Int) {
        runOnIoThread { mTodoGroupDao.deleteTodoGroup(groupId) }
    }

    fun insertTodo(todo: Todo) {
        runOnIoThread { mTodoDao.insertTodo(todo) }
    }

    fun insertTodoGroup(todoGroup: TodoGroup) {
        runOnIoThread { mTodoGroupDao.insertTodoGroup(todoGroup) }
    }

    fun updateTodoGroup(groupId: Int, updatedName: String) {
        runOnIoThread { mTodoGroupDao.updateTodoGroup(groupId, updatedName) }
    }

    companion object {
        @Suppress("unused")
        private val TAG by lazy { TodoRepository::class.java.simpleName }
        @Volatile private var mInstance: TodoRepository? = null

        fun getInstance(todoDao: TodoDao, todoGroupDao: TodoGroupDao) =
            mInstance ?: synchronized(this) {
                mInstance ?: TodoRepository(todoDao, todoGroupDao).also { mInstance = it }
            }
    }
}