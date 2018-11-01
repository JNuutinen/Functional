package com.github.jnuutinen.functional.presentation

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.data.db.entity.Todo
import kotlinx.android.synthetic.main.item_todo.view.*

class TodoAdapter(private val resources: Resources) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    @Suppress("PrivatePropertyName", "unused")
    private val mTAG by lazy { TodoAdapter::class.java.simpleName }
    private var mTodos: List<Todo>? = null
    var onItemClick: ((Todo) -> Unit)? = null

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val todo = mTodos?.get(adapterPosition)
                if (todo != null) onItemClick?.invoke(todo)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = mTodos?.get(position)
        if (todo != null) {
            val circle = ResourcesCompat.getDrawable(resources, R.drawable.circle, null)
            val color = todo.color
            circle?.setTint(color)
            holder.itemView.item_letter.background = circle
            holder.itemView.item_letter.text = todo.contents[0].toString()
            holder.itemView.item_text.text = todo.contents
        }
    }


    override fun getItemCount() = mTodos?.size ?: 0

    fun getItem(position: Int): Todo {
        return mTodos?.get(position) ?: Todo(0, "", 0, 0, 1)
    }

    fun setTodos(todos: List<Todo>) {
        if (mTodos == null) {
            mTodos = todos
            notifyItemRangeInserted(0, todos.size)
        } else {
            val oldTodos = mTodos
            if (oldTodos != null) {
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
                                && old.todoGroupId == new.todoGroupId
                    }
                })
                mTodos = todos
                result.dispatchUpdatesTo(this)
            }
        }
    }
}
