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
    private val TAG by lazy { TodoAdapter::class.java.simpleName }
    private var todos: MutableList<Todo>? = null

    inner class TodoViewHolder(itemView: View)  : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos?.get(position)
        if (todo != null) {
            val circle = ResourcesCompat.getDrawable(resources, R.drawable.circle, null)!!
            val color = todo.color
            circle.setTint(ResourcesCompat.getColor(resources, color, null))
            holder.itemView.item_letter.background = circle
            holder.itemView.item_letter.text = todo.contents[0].toString()
            holder.itemView.item_text.text = todo.contents
        }
    }

    override fun getItemCount() = todos?.size ?: 0

    fun getItem(position: Int): Todo {
        return todos!![position]
    }

    fun removeTodo(position: Int) {
        todos?.removeAt(position) ?: return
        notifyItemRemoved(position)
    }

    fun restoreTodo(todo: Todo, position: Int) {
        todos?.add(position, todo)
        notifyItemInserted(position)
    }

    fun setTodos(todos: MutableList<Todo>) {
        if (this.todos == null) {
            this.todos = todos
            notifyItemRangeInserted(0, todos.size)
        } else {
            val oldTodos = this.todos
            if (oldTodos != null) {
                val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int {
                        return oldTodos.size
                    }

                    override fun getNewListSize(): Int {
                        return todos.size
                    }

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
                this.todos = todos
                result.dispatchUpdatesTo(this)
            }
        }
    }
}
