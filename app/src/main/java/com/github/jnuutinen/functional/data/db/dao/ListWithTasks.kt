package com.github.jnuutinen.functional.data.db.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.github.jnuutinen.functional.data.db.entity.Task
import com.github.jnuutinen.functional.data.db.entity.TaskList

class ListWithTasks(@Embedded
                    val taskList: TaskList,

                    @Relation(parentColumn = "group_id", entityColumn = "todo_group_id", entity = Task::class)
                    val tasks: List<Task>)
