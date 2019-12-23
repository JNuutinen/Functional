package com.github.jnuutinen.functional.presentation

import android.content.res.Resources
import android.graphics.PorterDuff
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.data.db.entity.Task
import java.util.Collections
import kotlinx.android.synthetic.main.item_task.view.item_letter
import kotlinx.android.synthetic.main.item_task.view.item_text

class TaskAdapter(
    private val resources: Resources
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private lateinit var mTasks: MutableList<Task>
    private var mTasksInitialized = false
    var onItemClick: ((Task) -> Unit)? = null

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (mTasksInitialized) {
                    val task = mTasks[adapterPosition]
                    onItemClick?.invoke(task)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_task,
            parent,
            false
        )
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        if (mTasksInitialized) {
            val task = mTasks[position]
            val circle = ResourcesCompat.getDrawable(resources, R.drawable.circle, null)
            val color = task.color

            // setTint on drawable does not work on Lollipop (API level 21).
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                circle?.setTint(color)
            } else {
                circle?.mutate()?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
            holder.itemView.item_letter.background = circle
            holder.itemView.item_letter.text = task.contents[0].toString()
            holder.itemView.item_text.text = task.contents
        }
    }

    override fun getItemCount() = if (mTasksInitialized) mTasks.size else 0

    fun getItem(position: Int): Task {
        return if (mTasksInitialized) mTasks[position]
        else Task(0, "", 0, 0, 0, 1)
    }

    fun getItems() = mTasks

    fun onDelete(position: Int) {
        if (mTasksInitialized) {
            for (i in (position + 1) until mTasks.size) {
                val task = mTasks[i]
                task.order = task.order - 1
            }
        }
    }

    fun onDeleteUndo(position: Int) {
        if (mTasksInitialized) {
            for (i in position until mTasks.size) {
                val task = mTasks[i]
                task.order = task.order + 1
            }
        }
    }

    fun setTasks(tasks: MutableList<Task>) {
        tasks.sort()
        if (!mTasksInitialized) {
            mTasks = tasks
            mTasksInitialized = true
            notifyItemRangeInserted(0, tasks.size)
        } else {
            val oldTasks = mTasks
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = oldTasks.size

                override fun getNewListSize() = tasks.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldTasks[oldItemPosition].id == tasks[newItemPosition].id
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val old = oldTasks[oldItemPosition]
                    val new = tasks[newItemPosition]
                    return old.id == new.id &&
                        old.contents == new.contents &&
                        old.date == new.date &&
                        old.color == new.color &&
                        old.taskListId == new.taskListId
                }
            })
            mTasks = tasks
            result.dispatchUpdatesTo(this)
        }
    }

    fun swap(from: Int, to: Int) {
        // FIXME suboptimal, no need to update all indices on swap
        if (mTasksInitialized) {
            Collections.swap(mTasks, from, to)
            notifyItemMoved(from, to)
            for ((i, task) in mTasks.withIndex()) {
                task.order = i
            }
        }
    }
}
