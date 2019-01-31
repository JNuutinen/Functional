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
import com.github.jnuutinen.functional.data.db.entity.Todo
import kotlinx.android.synthetic.main.item_todo.view.*
import java.util.*

class TodoAdapter(private val resources: Resources) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    @Suppress("PrivatePropertyName", "unused")
    private val mTAG by lazy { TodoAdapter::class.java.simpleName }
    private lateinit var mTodos: MutableList<Todo>
    private var mTodosInitialized = false
    var onItemClick: ((Todo) -> Unit)? = null

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (mTodosInitialized) {
                    val todo = mTodos[adapterPosition]
                    onItemClick?.invoke(todo)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        if (mTodosInitialized) {
            val todo = mTodos[position]
            val circle = ResourcesCompat.getDrawable(resources, R.drawable.circle, null)
            val color = todo.color

            // setTint on drawable does not work on Lollipop (API level 21).
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                circle?.setTint(color)
            } else {
                circle?.mutate()?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
            holder.itemView.item_letter.background = circle
            holder.itemView.item_letter.text = todo.contents[0].toString()
            holder.itemView.item_text.text = todo.contents
        }
    }

    override fun getItemCount() = if (mTodosInitialized) mTodos.size else 0

    fun getItem(position: Int): Todo {
        return if (mTodosInitialized) mTodos[position] else Todo(0, "", 0, 0, 0, 1)
    }

    fun getItems() = mTodos

    fun onDelete(position: Int) {
        if (mTodosInitialized) {
            for (i in (position + 1) until mTodos.size) {
                val todo = mTodos[i]
                todo.order = todo.order - 1
            }
        }
    }

    fun onDeleteUndo(position: Int) {
        if (mTodosInitialized) {
            for (i in position until mTodos.size) {
                val todo = mTodos[i]
                todo.order = todo.order + 1
            }
        }
    }

    fun setTodos(todos: MutableList<Todo>) {
        todos.sort()
        if (!mTodosInitialized) {
            mTodos = todos
            mTodosInitialized = true
            notifyItemRangeInserted(0, todos.size)
        } else {
            val oldTodos = mTodos
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = oldTodos.size

                override fun getNewListSize() = todos.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return oldTodos[oldItemPosition].id == todos[newItemPosition].id
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val old = oldTodos[oldItemPosition]
                    val new = todos[newItemPosition]
                    return old.id == new.id
                            && old.contents == new.contents
                            && old.date == new.date
                            && old.color == new.color
                            && old.todoListId == new.todoListId
                }
            })
            mTodos = todos
            result.dispatchUpdatesTo(this)
        }
    }

    fun swap(from: Int, to: Int) {
        // FIXME suboptimal, no need to update all indices on swap
        if (mTodosInitialized) {
            Collections.swap(mTodos, from, to)
            notifyItemMoved(from, to)
            for ((i, todo) in mTodos.withIndex()) {
                todo.order = i
            }
        }
    }
}
