package com.github.jnuutinen.functional.presentation

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.data.db.dao.GroupWithTodos
import com.github.jnuutinen.functional.data.db.entity.Todo
import com.github.jnuutinen.functional.data.db.entity.TodoGroup
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todos)
        setSupportActionBar(toolbar)

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
        val itemDivider = TodoItemDivider(this)
        todo_recycler.apply {
            addItemDecoration(itemDivider)
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = mViewAdapter
        }

        readListPrefs()
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
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Check if clicked item was "Add list". Do comparison with the group id, because the list item's id can overlap
        // with the "Add list" item's id, because all the list item's ids are the same as their TodoGroup entities' ids
        // and the "Add list" item's id is defined in XML.
        if (item.groupId == R.id.group_add) {
            MaterialDialog(this)
                .message(R.string.action_add_list)
                .customView(R.layout.dialog_add_group, scrollable = true)
                .positiveButton(R.string.action_add_todo) { dialog ->
                    val customView = dialog.getCustomView()!!
                    val name = customView.findViewById<TextInputEditText>(R.id.add_group_text).text.toString().trim()
                    if (name.isEmpty()) {
                        Snackbar.make(main_coordinator, R.string.alert_list_name_empty, Snackbar.LENGTH_SHORT).show()
                    } else {
                        val group = TodoGroup(0, name, Calendar.getInstance().time.time)
                        mViewModel.insertTodoGroup(group)
                        // If active group is 0, then the most recent group will be set active in subscribeUi().
                        mViewModel.activeGroup = 0
                    }
                }
                .negativeButton(android.R.string.cancel)
                .show()
        } else {
            mViewModel.activeGroup = item.itemId
            forceViewModelLiveDataUpdate()
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun addTodo() {
        val customView = layoutInflater.inflate(R.layout.dialog_add_todo, main_coordinator, false)
        val colorButton = customView.findViewById<MaterialButton>(R.id.button_select_color)
        var selectedColor = ContextCompat.getColor(this, getRandomColor())
        colorButton.iconTint = ColorStateList.valueOf(selectedColor)
        colorButton.setOnClickListener {
            MaterialDialog(this)
                .colorChooser(mColorValues, initialSelection = selectedColor) { _, color ->
                    selectedColor = color
                    colorButton.iconTint = ColorStateList.valueOf(color)
                }
                .positiveButton(R.string.action_select)
                .negativeButton(android.R.string.cancel)
                .show()
        }
        MaterialDialog(this)
            .message(R.string.message_add_todo)
            .customView(view = customView, scrollable = true)
            .positiveButton(R.string.action_add_todo) { dialog ->
                val v = dialog.getCustomView()!!
                val date = Calendar.getInstance().time
                val content = v.findViewById<TextInputEditText>(R.id.add_todo_text).text.toString().trim()
                if (content.isEmpty()) {
                    Snackbar.make(main_coordinator, R.string.alert_todo_empty, Snackbar.LENGTH_SHORT).show()
                } else {
                    mViewModel.insertTodo(Todo(0, content, date.time, selectedColor, mViewModel.activeGroup))
                }
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun deleteList() {
        MaterialDialog(this)
            .message(R.string.message_delete_list)
            .positiveButton(R.string.action_delete) {
                mViewModel.deleteTodoGroup(mViewModel.activeGroup)
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun editList() {
        val customView = layoutInflater.inflate(R.layout.dialog_edit_group, main_coordinator, false)
        val textInput = customView.findViewById<TextInputEditText>(R.id.edit_group_text)
        val textLayout = customView.findViewById<TextInputLayout>(R.id.field_edit_group)
        textLayout.hint = getString(R.string.hint_list_name)
        textInput.setText(title.toString())
        textInput.setSelection(title.length)
        MaterialDialog(this)
            .message(R.string.message_edit_list)
            .customView(view = customView)
            .positiveButton(R.string.action_save) { dialog ->
                val v = dialog.getCustomView()!!
                val name = v.findViewById<TextInputEditText>(R.id.edit_group_text).text.toString().trim()
                if (name.isEmpty()) {
                    Snackbar.make(main_coordinator, R.string.alert_list_name_empty, Snackbar.LENGTH_SHORT).show()
                } else {
                    mViewModel.updateTodoGroup(mViewModel.activeGroup, name)
                }
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun editTodo(todo: Todo) {
        val customView = layoutInflater.inflate(R.layout.dialog_add_todo, main_coordinator, false)
        val textInput = customView.findViewById<TextInputEditText>(R.id.add_todo_text)
        val textLayout = customView.findViewById<TextInputLayout>(R.id.field_add_todo)
        textLayout.hint = getString(R.string.hint_todo)
        textInput.setText(todo.contents)
        textInput.setSelection(todo.contents.length)
        val colorButton = customView.findViewById<MaterialButton>(R.id.button_select_color)
        var selectedColor = todo.color
        colorButton.iconTint = ColorStateList.valueOf(selectedColor)
        colorButton.setOnClickListener {
            MaterialDialog(this)
                .colorChooser(mColorValues, initialSelection = selectedColor) { _, color ->
                    selectedColor = color
                    colorButton.iconTint = ColorStateList.valueOf(color)
                }
                .positiveButton(R.string.action_select)
                .negativeButton(android.R.string.cancel)
                .show()
        }
        MaterialDialog(this)
            .message(R.string.message_edit_todo)
            .customView(view = customView, scrollable = true)
            .positiveButton(R.string.action_save) { dialog ->
                val v = dialog.getCustomView()!!
                val content = v.findViewById<TextInputEditText>(R.id.add_todo_text).text.toString().trim()
                if (content.isEmpty()) {
                    Snackbar.make(main_coordinator, R.string.alert_todo_empty, Snackbar.LENGTH_SHORT).show()
                } else {
                    val updatedTodo = Todo(todo.id, content, todo.date, selectedColor, todo.todoGroupId)
                    mViewModel.insertTodo(updatedTodo)
                }
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun forceViewModelLiveDataUpdate() {
        mViewModel.groupsWithTodos.value = mViewModel.groupsWithTodos.value
    }

    private fun readListPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(PREF_KEY_ACTIVE_LIST_ID, PREF_VALUE_DOES_NOT_EXIST_INT)
        if (id != PREF_VALUE_DOES_NOT_EXIST_INT) mViewModel.activeGroup = id
    }

    private fun setGroup(groupWithTodos: GroupWithTodos) {
        val group = groupWithTodos.todoGroup
        val todos = groupWithTodos.todos
        mViewModel.activeGroup = group.id
        mViewAdapter.setTodos(todos)
        title = group.name
        if (todos.isEmpty()) text_no_todos.visibility = View.VISIBLE
        else text_no_todos.visibility = View.INVISIBLE

        // Set selected Navigation Drawer to-do list.
        val navItem = nav_view.menu.findItem(group.id)
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
            private val deleteIcon = ContextCompat.getDrawable(this@TodosActivity, R.drawable.ic_delete_white_24dp)!!
            val deleteIconMargin = resources.getDimension(R.dimen.item_background_delete_icon_margin).toInt()

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val itemHeight = itemView.height
                val intrinsicWidth = deleteIcon.intrinsicWidth
                val intrinsicHeight = deleteIcon.intrinsicHeight

                if (dX < 0) { // Item is being dragged to the left.
                    deletionBackground.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                    val deleteIconRight = itemView.right - deleteIconMargin
                    val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val deleteIconBottom = deleteIconTop + intrinsicHeight
                    deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                } else { // Item is being dragged to the right.
                    deletionBackground.setBounds(
                        itemView.left + dX.toInt(),
                        itemView.top,
                        itemView.left,
                        itemView.bottom
                    )
                    val deleteIconLeft = itemView.left + deleteIconMargin
                    val deleteIconRight = itemView.left + deleteIconMargin + intrinsicWidth
                    val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val deleteIconBottom = deleteIconTop + intrinsicHeight
                    deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                }
                deletionBackground.draw(c)
                deleteIcon.draw(c)

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
                if (parent.itemAnimator?.isRunning == true) {
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

    private fun subscribeUi() {
        mViewModel.groupsWithTodos.observe(this, Observer { groupsWithTodos ->
            // Groups will be in date order, from oldest to newest.

            // The lists menu is inside the main drawer menu.
            val listsMenu = nav_view.menu[1].subMenu
            listsMenu.clear()

            if (groupsWithTodos != null) {
                if (groupsWithTodos.isNotEmpty()) {
                    var groupIsSet = false
                    for (i in 0 until groupsWithTodos.size) {
                        // Handle groups.
                        val groupWithTodos = groupsWithTodos[i]
                        val group = groupWithTodos.todoGroup
                        listsMenu.add(R.id.group_lists, group.id, Menu.NONE, group.name)
                        val counter = layoutInflater.inflate(R.layout.menu_counter, drawer_layout, false)
                        counter.findViewById<TextView>(R.id.text_counter).text = groupWithTodos.todos.size.toString()
                        listsMenu[i].actionView = counter

                        if (mViewModel.activeGroup == 0 && i == groupsWithTodos.size - 1) {
                            // If active group is set to 0, then the most recent group must be set as active, in order
                            // to activate the newly added group.
                            setGroup(groupWithTodos)
                            groupIsSet = true
                        } else if (mViewModel.activeGroup == -1 || group.id == mViewModel.activeGroup) {
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
                    mViewModel.insertTodoGroup(group)
                }

                // Finally set the created nav drawer list group as exclusively checkable.
                // Event though it is impossible that multiple lists would be checked at the same time even if this was
                // not set, the items are highlighted more prominently when they are exclusively checkable.
                listsMenu.setGroupCheckable(R.id.group_lists, true, false)
            }
        })
    }

    private fun writeListPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(PREF_KEY_ACTIVE_LIST_ID, mViewModel.activeGroup) }
    }
}
