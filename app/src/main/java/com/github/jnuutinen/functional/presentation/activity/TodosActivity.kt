package com.github.jnuutinen.functional.presentation.activity

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.github.jnuutinen.functional.BuildConfig
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.data.db.dao.ListWithTodos
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoList
import com.github.jnuutinen.functional.presentation.TodoAdapter
import com.github.jnuutinen.functional.presentation.TodoItemDivider
import com.github.jnuutinen.functional.presentation.viewmodel.TodosViewModel
import com.github.jnuutinen.functional.util.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_todos.*
import kotlinx.android.synthetic.main.app_bar_todos.*
import kotlinx.android.synthetic.main.content_todos.*
import java.util.*

class TodosActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    @Suppress("PrivatePropertyName", "PrivatePropertyName", "unused")
    private val mTAG by lazy { TodosActivity::class.java.simpleName }
    private lateinit var mViewModel: TodosViewModel
    private lateinit var mViewAdapter: TodoAdapter
    private lateinit var mColorValues: IntArray
    private var mDeleteBgEnabled = true
    private lateinit var mDefaultListName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todos)
        setSupportActionBar(toolbar)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        mDefaultListName = getString(R.string.list_default_name)

        val factory = InjectorUtils.provideTodosViewModelFactory(this)
        mViewModel = ViewModelProviders.of(this, factory).get(TodosViewModel::class.java)

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
        mViewAdapter  = TodoAdapter(resources)
        mViewAdapter.onItemClick = { todo -> editTodo(todo) }
        val dividerItemDecoration = TodoItemDivider(ContextCompat.getColor(this, R.color.mainBackgroundColor),
            pxFromDp(this, 1f))
        todo_recycler.apply {
            addItemDecoration(dividerItemDecoration)
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = mViewAdapter
        }

        checkFirstRun()
        readPrefs()
        setUpItemTouchHelper()
        setUpAnimationDecoratorHelper()
        subscribeUi()

        // Build an IntArray with all available circle colors as int values.
        mColorValues = IntArray(colors.size) { i -> ContextCompat.getColor(this, colors[i]) }
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
            R.id.action_settings -> {
                startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_REQUEST)
                true
            }
            R.id.action_help -> {
                startActivity(Intent(this, IntroActivity::class.java))
                true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SETTINGS_REQUEST -> {
                writeListPrefs()
                readPrefs()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Check if clicked item was "Add list". Do comparison with the group id, because the list item's id can overlap
        // with the "Add list" item's id, because all the list item's ids are the same as their TodoList entities' ids
        // and the "Add list" item's id is defined in XML.
        if (item.groupId == R.id.group_add) {
            MaterialDialog(this)
                .message(R.string.action_add_list)
                .customView(R.layout.dialog_add_list, scrollable = true)
                .positiveButton(R.string.action_add_todo) { dialog ->
                    val customView = dialog.getCustomView()
                    val name = customView?.findViewById<TextInputEditText>(R.id.edit_list_add)?.text.toString().trim()
                    if (name.isEmpty()) {
                        Snackbar.make(main_coordinator, R.string.alert_list_name_empty, Snackbar.LENGTH_SHORT).show()
                    } else {
                        val list = TodoList(0, name, Calendar.getInstance().time.time)
                        mViewModel.insertTodoList(list)
                        // If active list is 0, then the most recent list will be set active in subscribeUi().
                        mViewModel.activeList = 0
                    }
                }
                .negativeButton(android.R.string.cancel)
                .show()
        } else {
            mViewModel.activeList = item.itemId
            forceViewModelLiveDataUpdate()
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun addTodo() {
        val customView = layoutInflater.inflate(R.layout.dialog_add_todo, main_coordinator, false)
        val colorButton = customView.findViewById<MaterialButton>(R.id.button_select_color)
        var selectedColor = ContextCompat.getColor(this, getRandomColor())
        setVersionAwareDrawableTint(colorButton.icon, selectedColor)
        colorButton.setOnClickListener {
            MaterialDialog(this)
                .colorChooser(mColorValues, initialSelection = selectedColor) { _, color ->
                    selectedColor = color
                    setVersionAwareDrawableTint(colorButton.icon, color)
                }
                .positiveButton(R.string.action_select)
                .negativeButton(android.R.string.cancel)
                .show()
        }
        MaterialDialog(this)
            .message(R.string.message_add_todo)
            .customView(view = customView, scrollable = true)
            .positiveButton(R.string.action_add_todo) { dialog ->
                val v = dialog.getCustomView()
                val date = Calendar.getInstance().time
                val content = v?.findViewById<TextInputEditText>(R.id.edit_todo_add)?.text.toString().trim()
                if (content.isEmpty()) {
                    Snackbar.make(main_coordinator, R.string.alert_todo_empty, Snackbar.LENGTH_SHORT).show()
                } else {
                    mViewModel.insertTodo(Todo(0, content, date.time, selectedColor, mViewModel.activeList))
                }
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun checkFirstRun() {
        val currentVersionCode = BuildConfig.VERSION_CODE
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedVersionCode = prefs.getInt(PREF_KEY_VERSION_CODE, PREF_VALUE_DOES_NOT_EXIST_INT)

        when {
            currentVersionCode == savedVersionCode -> return
            savedVersionCode == PREF_VALUE_DOES_NOT_EXIST_INT -> {
                // First install, show the introduction.
                startActivity(Intent(this, IntroActivity::class.java))
            }
            currentVersionCode > savedVersionCode -> {
                // Update.
                if (savedVersionCode == 1) {
                    // Show the introduction, if updating from version 1 (release 0.1.0).
                    startActivity(Intent(this, IntroActivity::class.java))
                }
            }
        }
        prefs.edit().putInt(PREF_KEY_VERSION_CODE, currentVersionCode).apply()
    }

    private fun deleteList() {
        MaterialDialog(this)
            .message(R.string.message_delete_list)
            .positiveButton(R.string.action_delete) { mViewModel.deleteTodoList(mViewModel.activeList) }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun editList() {
        val customView = layoutInflater.inflate(R.layout.dialog_edit_list, main_coordinator, false)
        val textInput = customView.findViewById<TextInputEditText>(R.id.edit_list_edit)
        val textLayout = customView.findViewById<TextInputLayout>(R.id.input_list_edit)
        textLayout.hint = getString(R.string.hint_list_name)
        textInput.setText(title.toString())
        textInput.setSelection(title.length)
        MaterialDialog(this)
            .message(R.string.message_edit_list)
            .customView(view = customView)
            .positiveButton(R.string.action_save) { dialog ->
                val v = dialog.getCustomView()
                val name = v?.findViewById<TextInputEditText>(R.id.edit_list_edit)?.text.toString().trim()
                if (name.isEmpty()) {
                    Snackbar.make(main_coordinator, R.string.alert_list_name_empty, Snackbar.LENGTH_SHORT).show()
                } else {
                    mViewModel.updateTodoList(mViewModel.activeList, name)
                }
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun editTodo(todo: Todo) {
        val customView = layoutInflater.inflate(R.layout.dialog_add_todo, main_coordinator, false)
        val textInput = customView.findViewById<TextInputEditText>(R.id.edit_todo_add)
        val textLayout = customView.findViewById<TextInputLayout>(R.id.input_todo_add)
        textLayout.hint = getString(R.string.hint_todo)
        textInput.setText(todo.contents)
        textInput.setSelection(todo.contents.length)
        val colorButton = customView.findViewById<MaterialButton>(R.id.button_select_color)
        var selectedColor = todo.color
        setVersionAwareDrawableTint(colorButton.icon, selectedColor)
        colorButton.setOnClickListener {
            MaterialDialog(this)
                .colorChooser(mColorValues, initialSelection = selectedColor) { _, color ->
                    selectedColor = color
                    setVersionAwareDrawableTint(colorButton.icon, color)
                }
                .positiveButton(R.string.action_select)
                .negativeButton(android.R.string.cancel)
                .show()
        }
        MaterialDialog(this)
            .message(R.string.message_edit_todo)
            .customView(view = customView, scrollable = true)
            .positiveButton(R.string.action_save) { dialog ->
                val v = dialog.getCustomView()
                val content = v?.findViewById<TextInputEditText>(R.id.edit_todo_add)?.text.toString().trim()
                if (content.isEmpty()) {
                    Snackbar.make(main_coordinator, R.string.alert_todo_empty, Snackbar.LENGTH_SHORT).show()
                } else {
                    val updatedTodo = Todo(todo.id, content, todo.date, selectedColor, todo.todoListId)
                    mViewModel.insertTodo(updatedTodo)
                }
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun forceViewModelLiveDataUpdate() {
        mViewModel.listsWithTodos.value = mViewModel.listsWithTodos.value
    }

    private fun readPrefs() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val id = prefs.getInt(PREF_KEY_ACTIVE_LIST_ID, PREF_VALUE_DOES_NOT_EXIST_INT)
        if (id != PREF_VALUE_DOES_NOT_EXIST_INT) mViewModel.activeList = id

        mDeleteBgEnabled = prefs.getBoolean(getString(R.string.pref_key_deletion_background), true)
        mDefaultListName = prefs.getString(getString(R.string.pref_key_default_list_name),
            getString(R.string.list_default_name)) ?: "My to-dos"
    }

    private fun setTodoList(listWithTodos: ListWithTodos) {
        val list = listWithTodos.todoList
        val todos = listWithTodos.todos
        mViewModel.activeList = list.id
        mViewAdapter.setTodos(todos)
        title = list.name
        if (todos.isEmpty()) text_no_todos.visibility = View.VISIBLE
        else text_no_todos.visibility = View.INVISIBLE

        // Set selected Navigation Drawer to-do list.
        val navItem = nav_view.menu.findItem(list.id)
        navItem.isChecked = true
        val counter = navItem.actionView.findViewById<TextView>(R.id.text_counter)

        // The list name color is automatically set to primaryColor when isChecked == true, but the counter ActionView
        // must be set manually.
        counter.setTextColor(ContextCompat.getColor(this, R.color.primaryColor))
    }

    /**
     * Adapted from here:
     *
     * https://stackoverflow.com/a/34740127/10518087
     * https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete/
     */
    private fun setUpItemTouchHelper() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            private val deletionBackground = ColorDrawable(ContextCompat.getColor(this@TodosActivity, R.color.negativeColor))
            private val deleteIcon = ContextCompat.getDrawable(this@TodosActivity, R.drawable.ic_delete_white_24dp)
            private val deleteIconMargin = resources.getDimension(R.dimen.item_background_delete_icon_margin).toInt()

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (mDeleteBgEnabled) {
                    val itemView = viewHolder.itemView
                    val itemHeight = itemView.height
                    val intrinsicWidth = deleteIcon?.intrinsicWidth ?: 0
                    val intrinsicHeight = deleteIcon?.intrinsicHeight ?: 0

                    if (dX < 0) {
                        // Item is being dragged to the left.
                        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                        val deleteIconRight = itemView.right - deleteIconMargin
                        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                        val deleteIconBottom = deleteIconTop + intrinsicHeight
                        deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                    } else {
                        // Item is being dragged to the right.
                        val deleteIconLeft = itemView.left + deleteIconMargin
                        val deleteIconRight = itemView.left + deleteIconMargin + intrinsicWidth
                        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                        val deleteIconBottom = deleteIconTop + intrinsicHeight
                        deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                    }
                    deletionBackground.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    deletionBackground.draw(c)
                    deleteIcon?.draw(c)
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedTodo = mViewAdapter.getItem(viewHolder.adapterPosition)
                mViewModel.deleteTodo(deletedTodo)
                val undoSnackbar = Snackbar.make(main_coordinator, R.string.alert_todo_deleted, Snackbar.LENGTH_LONG)
                undoSnackbar.setAction(R.string.action_undo) { mViewModel.insertTodo(deletedTodo) }
                undoSnackbar.show()
            }
        }
        ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(todo_recycler)
    }

    /**
     * Adapted from here:
     *
     * https://stackoverflow.com/a/34740127/10518087
     * https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete/
     */
    private fun setUpAnimationDecoratorHelper() {
        todo_recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            private val background = ColorDrawable(ContextCompat.getColor(this@TodosActivity, R.color.negativeColor))

            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.itemAnimator?.isRunning == true && mDeleteBgEnabled) {
                    var lastViewComingDown: View? = null
                    var firstViewComingUp: View? = null

                    val left = 0
                    val right = parent.width

                    var top = 0
                    var bottom = 0

                    val childCount = parent.layoutManager?.childCount ?: 0
                    for (i in 0 until childCount) {
                        val child = parent.layoutManager?.getChildAt(i)
                        if (child != null && i < childCount - 1 && child.translationY < 0) {
                            // View is coming down.
                            lastViewComingDown = child
                        } else if (child != null && child.translationY > 0) {
                            // View is coming up.
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // Views are going up and down to fill the void.
                        top = lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                        bottom = firstViewComingUp.top + firstViewComingUp.translationY.toInt()
                    } else if (lastViewComingDown != null) {
                        // Views are going down to fill the void.
                        top = lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                        bottom = lastViewComingDown.bottom
                    } else if (firstViewComingUp != null) {
                        // Views are going up to fill the void.
                        top = firstViewComingUp.top
                        bottom = firstViewComingUp.top + firstViewComingUp.translationY.toInt()
                    }

                    background.setBounds(left, top, right, bottom)
                    background.draw(c)
                }
                super.onDraw(c, parent, state)
            }
        })
    }

    private fun setVersionAwareDrawableTint(drawable: Drawable?, color: Int) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            drawable?.setTint(color)
        } else {
            drawable?.mutate()?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun subscribeUi() {
        mViewModel.listsWithTodos.observe(this, Observer { listsWithTodos ->
            // Lists will be in date order, from oldest to newest.

            // The lists menu is inside the main drawer menu.
            val listsMenu = nav_view.menu[1].subMenu
            listsMenu.clear()

            if (listsWithTodos != null) {
                if (listsWithTodos.isNotEmpty()) {
                    var listIsSet = false
                    for (i in 0 until listsWithTodos.size) {
                        // Handle lists.
                        val listWithTodos = listsWithTodos[i]
                        val list = listWithTodos.todoList
                        listsMenu.add(R.id.group_lists, list.id, Menu.NONE, list.name)
                        val counter = layoutInflater.inflate(R.layout.menu_counter, drawer_layout, false)
                        counter.findViewById<TextView>(R.id.text_counter).text = listWithTodos.todos.size.toString()
                        listsMenu[i].actionView = counter

                        if (mViewModel.activeList == 0 && i == listsWithTodos.size - 1) {
                            // If active list is set to 0, then the most recent list must be set as active, in order
                            // to activate the newly added list.
                            setTodoList(listWithTodos)
                            listIsSet = true
                        } else if (mViewModel.activeList == -1 || list.id == mViewModel.activeList) {
                            // Alternatively, if active list is not set or it is set to this list, set it as active.
                            setTodoList(listWithTodos)
                            listIsSet = true
                        }
                    }

                    // If list was not set during loop (active list was deleted), set it to the first list.
                    if (!listIsSet) setTodoList(listsWithTodos[0])
                } else {
                    // No lists; create an empty list with the default name from preferences.
                    val list = TodoList(1, mDefaultListName, Calendar.getInstance().time.time)
                    mViewModel.insertTodoList(list)
                }

                // Finally set the created nav drawer list group as exclusively checkable.
                listsMenu.setGroupCheckable(R.id.group_lists, true, true)
            }
        })
    }

    /**
     * Save the current active list to shared preferences, so that it will be opened when the user returns.
     */
    private fun writeListPrefs() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit { putInt(PREF_KEY_ACTIVE_LIST_ID, mViewModel.activeList) }
    }
}
