package com.github.jnuutinen.functional.presentation.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.github.jnuutinen.functional.data.TaskRepository
import com.github.jnuutinen.functional.data.db.dao.ListWithTasks
import com.github.jnuutinen.functional.data.db.entity.Task
import com.github.jnuutinen.functional.data.db.entity.TaskList
import com.github.jnuutinen.functional.util.Constants

class TasksViewModel internal constructor(
    private val mTaskRepository: TaskRepository
) : ViewModel() {

    // Use MediatorLiveData so that we can force update it from TasksActivity with
    // setValue(getValue).
    val listsWithTasks = MediatorLiveData<List<ListWithTasks>>()
    var activeList = Constants.PREF_VALUE_DOES_NOT_EXIST_INT

    init {
        listsWithTasks.addSource(mTaskRepository.getListsWithTasks()) { value ->
            listsWithTasks.value = value
        }
    }

    fun deleteTaskList(listId: Int) {
        mTaskRepository.deleteTaskList(listId)
    }

    fun insertTask(task: Task) {
        mTaskRepository.insertTask(task)
    }

    fun insertTaskList(taskList: TaskList) {
        mTaskRepository.insertTaskList(taskList)
    }

    fun onTaskDelete(positionsToUpdate: List<Task>, deletedTask: Task) {
        mTaskRepository.onTaskDelete(positionsToUpdate, deletedTask)
    }

    fun onTaskDeleteUndo(positionsToUpdate: List<Task>, insertedTask: Task) {
        mTaskRepository.onTaskDeleteUndo(positionsToUpdate, insertedTask)
    }

    fun updateTaskList(listId: Int, updatedName: String) {
        mTaskRepository.updateTaskList(listId, updatedName)
    }

    fun updateTasks(tasks: List<Task>) {
        mTaskRepository.updateTasks(tasks)
    }
}
