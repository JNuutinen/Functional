package com.github.jnuutinen.functional.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
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
    private val TAG by lazy { TodosActivity::class.java.simpleName }
    private lateinit var viewModel: TodosViewModel
    private lateinit var viewAdapter: TodoAdapter
    private var undoSnackbar: Snackbar? = null

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
        subscribeUiForTodoGroup(viewModel.activeGroup)
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
            // TODO
            R.id.action_edit_list -> {
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
            -1 -> {
                MaterialDialog(this)
                    .message(R.string.action_add_list)
                    .input { _, text ->
                        val group = TodoGroup(0, text.toString().capitalize(), Calendar.getInstance().time.time)
                        viewModel.insertTodoGroup(group)
                        viewModel.activeGroup = group.id
                        title = group.name
                        subscribeUiForTodoGroup(group.id)
                    }
                    .positiveButton(R.string.action_add_todo)
                    .negativeButton(android.R.string.cancel)
                    .show()
            }
            else -> {
                Log.e(TAG, "itemId: ${item.itemId}")
                viewModel.activeGroup = item.itemId
                subscribeUiForTodoGroup(viewModel.activeGroup)
                title = item.title
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun addTodo() {
        MaterialDialog(this)
            .message(R.string.message_add_todo)
            .input { _, text ->
                val date = Calendar.getInstance().time
                Log.e(TAG, "${viewModel.activeGroup}")
                viewModel.insertTodo(Todo(0, text.toString().capitalize(), date.time, getRandomColor(), viewModel.activeGroup))
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

    private fun readListPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(PREF_KEY_ACTIVE_LIST_ID, -1)
        val name = prefs.getString(PREF_KEY_ACTIVE_LIST_NAME, "")
        if (id != -1 && name != null && name.isNotBlank()) {
            viewModel.activeGroup = id
            title = name
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
                // TODO: fix rapid deletions
                // If Snackbar is visible (a delete is pending), dismiss it to delete the pending item.
                undoSnackbar?.dismiss()

                val deletedTodo = viewAdapter.getItem(viewHolder.adapterPosition)
                val deletedIndex = viewHolder.adapterPosition
                viewAdapter.removeTodo(viewHolder.adapterPosition)
                undoSnackbar = Snackbar.make(main_coordinator, R.string.alert_todo_deleted, Snackbar.LENGTH_LONG)
                undoSnackbar?.setAction(R.string.action_undo) { viewAdapter.restoreTodo(deletedTodo, deletedIndex) }
                undoSnackbar?.addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            viewModel.deleteTodo(deletedTodo)
                        }
                        super.onDismissed(transientBottomBar, event)
                    }
                })
                undoSnackbar?.show()
            }
        }
        ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(todo_recycler)
    }

    private fun subscribeUi() {
        viewModel.todoGroups.observe(this, Observer { todoGroups ->
            nav_view.menu.clear()
            nav_view.menu.add(R.id.group_add, -1, 0, R.string.action_add_list)
            nav_view.menu[0].icon = ContextCompat.getDrawable(this, R.drawable.ic_add_black_24dp)
            if (todoGroups != null) {
                if (todoGroups.isNotEmpty()) {
                    for (i in 0 until todoGroups.size) {
                        Log.e(TAG, "Setting id: ${todoGroups[i].id}")
                        nav_view.menu.add(R.id.group_lists, todoGroups[i].id, Menu.NONE, todoGroups[i].name)

                        // Set activity title, if this group is the currently active one.
                        if (todoGroups[i].id == viewModel.activeGroup) title = todoGroups[i].name
                    }
                } else {
                    // No groups; create an empty group and set it as active.
                    val group = TodoGroup(1, resources.getString(R.string.group_default_name), Calendar.getInstance().time.time)
                    viewModel.insertTodoGroup(group)
                    viewModel.activeGroup = group.id
                    title = group.name
                }
            }
        })
    }

    private fun subscribeUiForTodoGroup(activeGroup: Int) {
        viewModel.getTodosInGroup(activeGroup).observe(this, Observer { todos ->
            if (todos != null) {
                viewAdapter.setTodos(todos.toMutableList())
                if (todos.isEmpty()) text_no_todos.visibility = VISIBLE
                else text_no_todos.visibility = INVISIBLE
            }
        })
    }

    private fun writeListPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(PREF_KEY_ACTIVE_LIST_ID, viewModel.activeGroup) }
        prefs.edit { putString(PREF_KEY_ACTIVE_LIST_NAME, title.toString()) }
    }
}
