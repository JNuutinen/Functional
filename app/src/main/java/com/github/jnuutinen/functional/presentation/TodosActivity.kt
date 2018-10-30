package com.github.jnuutinen.functional.presentation

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.data.db.dao.GroupWithTodos
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoGroup
import com.github.jnuutinen.functional.util.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_todos.*
import kotlinx.android.synthetic.main.app_bar_todos.*
import kotlinx.android.synthetic.main.content_todos.*
import java.util.*

class TodosActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    @Suppress("PrivatePropertyName", "PrivatePropertyName", "unused")
    private val TAG by lazy { TodosActivity::class.java.simpleName }
    private lateinit var viewModel: TodosViewModel
    private lateinit var viewAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todos)
        setSupportActionBar(toolbar)

        val factory = InjectorUtils.provideTodosViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, factory).get(TodosViewModel::class.java)

        button_add_todo.setOnClickListener { addTodo() }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val viewManager = LinearLayoutManager(this)
        viewAdapter  = TodoAdapter(resources)
        val itemDivider = TodoItemDivider(this)
        todo_recycler.apply {
            addItemDecoration(itemDivider)
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        readListPrefs()
        setupItemTouchHelper()
        subscribeUi()
    }

    override fun onDestroy() {
        writeListPrefs()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_todos, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_list -> {
                editList()
                true
            }
            R.id.action_delete_list -> {
                deleteList()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            NAV_ADD_LIST_ID -> {
                MaterialDialog(this)
                    .message(R.string.action_add_list)
                    .input(hintRes = R.string.hint_list_name) { _, text ->
                        val name = text.toString().trim().capitalize()
                        if (name.isEmpty()) {
                            Snackbar.make(main_coordinator, R.string.alert_list_name_empty, Snackbar.LENGTH_SHORT).show()
                        } else {
                            val group = TodoGroup(0, name, Calendar.getInstance().time.time)
                            viewModel.insertTodoGroup(group)
                            // If active group is 0, then the most recent group will be set active in subscribeUi().
                            viewModel.activeGroup = 0
                        }
                    }
                    .positiveButton(R.string.action_add_todo)
                    .negativeButton(android.R.string.cancel)
                    .show()
            }
            else -> {
                viewModel.activeGroup = item.itemId
                viewModel.groupsWithTodos.value = viewModel.groupsWithTodos.value
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun addTodo() {
        MaterialDialog(this)
            .message(R.string.message_add_todo)
            .input(hintRes = R.string.hint_todo) { _, text ->
                val date = Calendar.getInstance().time
                val content = text.toString().trim().capitalize()
                if (content.isEmpty()) {
                    Snackbar.make(main_coordinator, R.string.alert_todo_empty, Snackbar.LENGTH_SHORT).show()
                } else {
                    viewModel.insertTodo(Todo(0, content, date.time, getRandomColor(), viewModel.activeGroup))
                }
            }
            .positiveButton(R.string.action_add_todo)
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun deleteList() {
        MaterialDialog(this)
            .message(R.string.message_delete_list)
            .positiveButton(R.string.action_delete) {
                viewModel.deleteTodoGroup(viewModel.activeGroup)
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun editList() {
        MaterialDialog(this)
            .message(R.string.message_edit_list)
            .input(hint = title.toString(), prefill = title.toString()) { _, text ->
                val name = text.toString().trim().capitalize()
                if (name.isEmpty()) {
                    Snackbar.make(main_coordinator, R.string.alert_list_name_empty, Snackbar.LENGTH_SHORT).show()
                } else {
                    viewModel.updateTodoGroup(viewModel.activeGroup, name)
                }
            }
            .positiveButton(R.string.action_save)
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun readListPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(PREF_KEY_ACTIVE_LIST_ID, -1)
        val name = prefs.getString(PREF_KEY_ACTIVE_LIST_NAME, "")
        if (id != -1 && name != null && name.isNotBlank()) {
            viewModel.activeGroup = id
        }
    }

    private fun setGroup(groupWithTodos: GroupWithTodos) {
        val group = groupWithTodos.todoGroup
        val todos = groupWithTodos.todos
        title = group.name
        nav_view.menu.findItem(group.id).isChecked = true
        viewModel.activeGroup = group.id
        viewAdapter.setTodos(todos)
        if (todos.isEmpty()) {
            //image_no_todos.visibility = View.VISIBLE
            text_no_todos.visibility = View.VISIBLE
        } else {
            //image_no_todos.visibility = View.INVISIBLE
            text_no_todos.visibility = View.INVISIBLE
        }
    }

    private fun setupItemTouchHelper() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedTodo = viewAdapter.getItem(viewHolder.adapterPosition)
                viewModel.deleteTodo(deletedTodo)
                val undoSnackbar = Snackbar.make(main_coordinator, R.string.alert_todo_deleted, Snackbar.LENGTH_LONG)
                undoSnackbar.setAction(R.string.action_undo) { viewModel.insertTodo(deletedTodo) }
                undoSnackbar.show()
            }
        }
        ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(todo_recycler)
    }

    private fun subscribeUi() {
        viewModel.groupsWithTodos.observe(this, Observer { groupsWithTodos ->
            // Groups will be in date order, from oldest to newest.

            nav_view.menu.clear()
            nav_view.menu.add(R.id.group_add, NAV_ADD_LIST_ID, 0, R.string.action_add_list)
            nav_view.menu[0].icon = ContextCompat.getDrawable(this, R.drawable.ic_add_black_24dp)
            if (groupsWithTodos != null) {
                if (groupsWithTodos.isNotEmpty()) {
                    var groupIsSet = false
                    for (i in 0 until groupsWithTodos.size) {
                        // Handle groups.
                        val groupWithTodos = groupsWithTodos[i]
                        val group = groupWithTodos.todoGroup
                        nav_view.menu.add(R.id.group_lists, group.id, Menu.NONE, group.name)

                        if (viewModel.activeGroup == 0 && i == groupsWithTodos.size - 1) {
                            // If active group is set to 0, then the most recent group must be set as active, in order
                            // to activate the newly added group.
                            setGroup(groupWithTodos)
                            groupIsSet = true
                        } else if (viewModel.activeGroup == -1 || group.id == viewModel.activeGroup) {
                            // Alternatively, if active group is not set or it is set to this group, set it as active.
                            setGroup(groupWithTodos)
                            groupIsSet = true
                        }
                    }

                    // If group was not set during loop (active group was deleted), set it to the first group.
                    if (!groupIsSet) setGroup(groupsWithTodos[0])
                } else {
                    // No groups; create an empty group.
                    val group = TodoGroup(1, resources.getString(R.string.group_default_name), Calendar.getInstance().time.time)
                    viewModel.insertTodoGroup(group)
                }

                // Finally set the created nav drawer list group as exclusively checkable.
                // Event though it is impossible that multiple lists would be checked at the same time even if this was
                // not set, the items are highlighted more prominently when they are exclusively checkable.
                nav_view.menu.setGroupCheckable(R.id.group_lists, true, false)
            }
        })
    }

    private fun writeListPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(PREF_KEY_ACTIVE_LIST_ID, viewModel.activeGroup) }
        prefs.edit { putString(PREF_KEY_ACTIVE_LIST_NAME, title.toString()) }
    }
}
