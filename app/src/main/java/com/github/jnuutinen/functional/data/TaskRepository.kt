package com.github.jnuutinen.functional.data

import com.github.jnuutinen.functional.data.db.dao.TaskDao
import com.github.jnuutinen.functional.data.db.dao.TaskListDao
import com.github.jnuutinen.functional.data.db.entity.Task
import com.github.jnuutinen.functional.data.db.entity.TaskList
import com.github.jnuutinen.functional.util.runOnIoThread

class TaskRepository private constructor(
    private val mTaskDao: TaskDao,
    private val mTaskListDao: TaskListDao
) {
    fun copyTaskList(listId: Int, newListName: String) {
        runOnIoThread { mTaskListDao.createCopy(listId, newListName) }
    }

    fun getListsWithTasks() = mTaskListDao.getAllListsWithTasks()

    fun deleteTaskList(listId: Int) {
        runOnIoThread { mTaskListDao.delete(listId) }
    }

    fun insertTask(task: Task) {
        runOnIoThread { mTaskDao.insert(task) }
    }

    fun insertTaskList(taskList: TaskList) {
        runOnIoThread { mTaskListDao.insert(taskList) }
    }

    fun onTaskDelete(positionsToUpdate: List<Task>, deletedTask: Task) {
        runOnIoThread {
            mTaskDao.updateAll(positionsToUpdate)
            mTaskDao.delete(deletedTask)
        }
    }

    fun onTaskDeleteUndo(positionsToUpdate: List<Task>, insertedTask: Task) {
        runOnIoThread {
            mTaskDao.updateAll(positionsToUpdate)
            mTaskDao.insert(insertedTask)
        }
    }

    fun updateTaskList(listId: Int, updatedName: String) {
        runOnIoThread { mTaskListDao.update(listId, updatedName) }
    }

    fun updateTasks(tasks: List<Task>) {
        runOnIoThread { mTaskDao.updateAll(tasks) }
    }

    companion object {
        @Suppress("unused")
        private val TAG by lazy { TaskRepository::class.java.simpleName }
        @Volatile private var mInstance: TaskRepository? = null

        fun getInstance(taskDao: TaskDao, taskListDao: TaskListDao) =
            mInstance ?: synchronized(this) {
                mInstance ?: TaskRepository(taskDao, taskListDao).also { mInstance = it }
            }
    }
}
