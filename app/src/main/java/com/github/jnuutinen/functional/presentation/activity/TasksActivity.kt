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
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.github.jnuutinen.functional.BuildConfig
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.data.db.dao.ListWithTasks
import com.github.jnuutinen.functional.data.db.entity.Task
import com.github.jnuutinen.functional.data.db.entity.TaskList
import com.github.jnuutinen.functional.presentation.TaskAdapter
import com.github.jnuutinen.functional.presentation.TaskItemDivider
import com.github.jnuutinen.functional.presentation.viewmodel.TasksViewModel
import com.github.jnuutinen.functional.util.ColorUtil
import com.github.jnuutinen.functional.util.Constants
import com.github.jnuutinen.functional.util.InjectorUtils
import com.github.jnuutinen.functional.util.MiscUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_tasks.drawer_layout
import kotlinx.android.synthetic.main.activity_tasks.nav_view
import kotlinx.android.synthetic.main.app_bar_tasks.button_add_task
import kotlinx.android.synthetic.main.app_bar_tasks.main_coordinator
import kotlinx.android.synthetic.main.app_bar_tasks.toolbar
import kotlinx.android.synthetic.main.content_tasks.task_recycler
import kotlinx.android.synthetic.main.content_tasks.text_no_tasks

class TasksActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mViewModel: TasksViewModel
    private lateinit var mViewAdapter: TaskAdapter
    private lateinit var mColorValues: IntArray
    private var mDeleteBgEnabled = true
    private lateinit var mDefaultListName: String
    private var mDragging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)
        setSupportActionBar(toolbar)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        mDefaultListName = getString(R.string.list_default_name)

        val factory = InjectorUtils.provideTasksViewModelFactory(this)
        mViewModel = ViewModelProviders.of(this, factory).get(TasksViewModel::class.java)

        button_add_task.setOnClickListener { addTask() }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val viewManager = LinearLayoutManager(this)
        mViewAdapter = TaskAdapter(resources)
        mViewAdapter.onItemClick = { task -> editTask(task) }
        val dividerItemDecoration = TaskItemDivider(
            ContextCompat.getColor(this, R.color.mainBackgroundColor),
            MiscUtil.pxFromDp(this, 1f)
        )
        task_recycler.apply {
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
        mColorValues = IntArray(ColorUtil.colors.size) { i ->
            ContextCompat.getColor(this, ColorUtil.colors[i])
        }
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
        menuInflater.inflate(R.menu.menu_tasks, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit_list -> {
                editList()
                true
            }
            R.id.action_copy_list -> {
                copyList()
                true
            }
            R.id.action_delete_list -> {
                deleteList()
                true
            }
            R.id.action_settings -> {
                startActivityForResult(
                    Intent(this, SettingsActivity::class.java),
                    Constants.SETTINGS_REQUEST
                )
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
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.SETTINGS_REQUEST -> {
                writeListPrefs()
                readPrefs()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Check if clicked item was "Add list". Do comparison with the group id, because the list
        // item's id can overlap with the "Add list" item's id, because all the list item's ids are
        // the same as their TaskList entities' ids and the "Add list" item's id is defined in XML.
        if (item.groupId == R.id.group_add) {
            MaterialDialog(this)
                .message(R.string.action_add_list)
                .customView(R.layout.dialog_add_list, scrollable = true)
                .positiveButton(R.string.action_add_task) { dialog ->
                    val customView = dialog.getCustomView()
                    val name = customView?.findViewById<TextInputEditText>(R.id.edit_list_add)
                        ?.text.toString().trim()
                    if (name.isEmpty()) {
                        Snackbar.make(
                            main_coordinator,
                            R.string.alert_list_name_empty,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        // If active list is 0, then the most recent list will be set active in
                        // subscribeUi().
                        mViewModel.activeList = 0
                        val list = TaskList(0, name, MiscUtil.getCurrentTime())
                        mViewModel.insertTaskList(list)
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

    private fun addTask() {
        val customView = layoutInflater.inflate(R.layout.dialog_add_task, main_coordinator, false)
        val colorButton = customView.findViewById<MaterialButton>(R.id.button_select_color)
        var selectedColor = ContextCompat.getColor(this, ColorUtil.getRandomColor())
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
            .message(R.string.message_add_task)
            .customView(view = customView, scrollable = true)
            .positiveButton(R.string.action_add_task) { dialog ->
                val v = dialog.getCustomView()
                val content = v?.findViewById<TextInputEditText>(R.id.edit_task_add)
                    ?.text.toString().trim()
                if (content.isEmpty()) {
                    Snackbar
                        .make(main_coordinator, R.string.alert_task_empty, Snackbar.LENGTH_SHORT)
                        .show()
                } else {
                    mViewModel.insertTask(
                        Task(
                            0,
                            content,
                            MiscUtil.getCurrentTime(),
                            selectedColor,
                            mViewAdapter.itemCount,
                            mViewModel.activeList
                        )
                    )
                }
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun copyList() {
        val currentListId = mViewModel.activeList
        val newListName = getString(R.string.text_copied_list_name, title)
        mViewModel.activeList = 0
        mViewModel.copyTaskList(currentListId, newListName)
    }

    private fun checkFirstRun() {
        val currentVersionCode = BuildConfig.VERSION_CODE
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val savedVersionCode = prefs.getInt(
            Constants.PREF_KEY_VERSION_CODE,
            Constants.PREF_VALUE_DOES_NOT_EXIST_INT
        )

        when {
            currentVersionCode == savedVersionCode -> return
            savedVersionCode == Constants.PREF_VALUE_DOES_NOT_EXIST_INT -> {
                // First install, show the introduction.
                startActivity(Intent(this, IntroActivity::class.java))
            }
            currentVersionCode > savedVersionCode -> {
                // Update.
                if (savedVersionCode == 1) {
                    // Show the introduction, if updating from version 1 (release 0.1.0).
                    startActivity(Intent(this, IntroActivity::class.java))
                } else if (savedVersionCode < 6) {
                    // Show the drag & drop slide, if updating from version <= 5
                    // (release 0.2.3 or earlier).
                    val intent = Intent(this, IntroActivity::class.java)
                    intent.putExtra(
                        Constants.EXTRA_NAME_INTRO_SLIDE,
                        Constants.EXTRA_VALUE_INTRO_DRAG_DROP
                    )
                    startActivity(intent)
                }
            }
        }
        prefs.edit().putInt(Constants.PREF_KEY_VERSION_CODE, currentVersionCode).apply()
    }

    private fun deleteList() {
        MaterialDialog(this)
            .message(R.string.message_delete_list)
            .positiveButton(R.string.action_delete) {
                mViewModel.deleteTaskList(mViewModel.activeList)
            }
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
                val name = v
                    ?.findViewById<TextInputEditText>(R.id.edit_list_edit)
                    ?.text
                    .toString()
                    .trim()
                if (name.isEmpty()) {
                    Snackbar.make(
                        main_coordinator, R.string.alert_list_name_empty,
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    mViewModel.updateTaskList(mViewModel.activeList, name)
                }
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun editTask(task: Task) {
        val customView = layoutInflater.inflate(R.layout.dialog_add_task, main_coordinator, false)
        val textInput = customView.findViewById<TextInputEditText>(R.id.edit_task_add)
        val textLayout = customView.findViewById<TextInputLayout>(R.id.input_task_add)
        textLayout.hint = getString(R.string.hint_task)
        textInput.setText(task.contents)
        textInput.setSelection(task.contents.length)
        val colorButton = customView.findViewById<MaterialButton>(R.id.button_select_color)
        var selectedColor = task.color
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
            .message(R.string.message_edit_task)
            .customView(view = customView, scrollable = true)
            .positiveButton(R.string.action_save) { dialog ->
                val v = dialog.getCustomView()
                val content = v
                    ?.findViewById<TextInputEditText>(R.id.edit_task_add)
                    ?.text
                    .toString()
                    .trim()
                if (content.isEmpty()) {
                    Snackbar.make(
                        main_coordinator,
                        R.string.alert_task_empty,
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    val updatedTask = Task(
                        task.id,
                        content,
                        task.date,
                        selectedColor,
                        task.order,
                        task.taskListId
                    )
                    mViewModel.insertTask(updatedTask)
                }
            }
            .negativeButton(android.R.string.cancel)
            .show()
    }

    private fun forceViewModelLiveDataUpdate() {
        mViewModel.listsWithTasks.value = mViewModel.listsWithTasks.value
    }

    private fun readPrefs() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val id = prefs.getInt(
            Constants.PREF_KEY_ACTIVE_LIST_ID,
            Constants.PREF_VALUE_DOES_NOT_EXIST_INT
        )
        if (id != Constants.PREF_VALUE_DOES_NOT_EXIST_INT) mViewModel.activeList = id

        mDeleteBgEnabled = prefs.getBoolean(getString(R.string.pref_key_deletion_background), true)
        mDefaultListName = prefs.getString(
            getString(R.string.pref_key_default_list_name),
            getString(R.string.list_default_name)
        ) ?: "My tasks"
    }

    private fun setTaskList(listWithTasks: ListWithTasks) {
        val list = listWithTasks.taskList
        val tasks = listWithTasks.tasks
        mViewModel.activeList = list.id
        mViewAdapter.setTasks(tasks.toMutableList())
        title = list.name
        if (tasks.isEmpty()) text_no_tasks.visibility = View.VISIBLE
        else text_no_tasks.visibility = View.INVISIBLE

        // Set selected Navigation Drawer task list.
        val navItem = nav_view.menu.findItem(list.id)
        navItem.isChecked = true
        val counter = navItem.actionView.findViewById<TextView>(R.id.text_counter)

        // The list name color is automatically set to primaryColor when isChecked == true, but the
        // counter ActionView must be set manually.
        counter.setTextColor(ContextCompat.getColor(this, R.color.primaryColor))
    }

    /**
     * Adapted from here:
     *
     * https://stackoverflow.com/a/34740127/10518087
     * https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete/
     */
    private fun setUpItemTouchHelper() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
            ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
        ) {
            private val deletionBackground = ColorDrawable(
                ContextCompat.getColor(
                    this@TasksActivity,
                    R.color.negativeColor
                )
            )
            private val deleteIcon = ContextCompat.getDrawable(
                this@TasksActivity,
                R.drawable.ic_delete_white_24dp
            )
            private val deleteIconMargin = resources.getDimension(
                R.dimen.item_background_delete_icon_margin
            ).toInt()

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                mDragging = false
                mViewModel.updateTasks(mViewAdapter.getItems())
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (mDeleteBgEnabled && !mDragging) {
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
                        deleteIcon?.setBounds(
                            deleteIconLeft,
                            deleteIconTop,
                            deleteIconRight,
                            deleteIconBottom
                        )
                    } else {
                        // Item is being dragged to the right.
                        val deleteIconLeft = itemView.left + deleteIconMargin
                        val deleteIconRight = itemView.left + deleteIconMargin + intrinsicWidth
                        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                        val deleteIconBottom = deleteIconTop + intrinsicHeight
                        deleteIcon?.setBounds(
                            deleteIconLeft,
                            deleteIconTop,
                            deleteIconRight,
                            deleteIconBottom
                        )
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
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX, dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                mViewAdapter.swap(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ACTION_STATE_DRAG) {
                    mDragging = true
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedTask = mViewAdapter.getItem(viewHolder.adapterPosition)
                mViewAdapter.onDelete(deletedTask.order)
                mViewModel.onTaskDelete(mViewAdapter.getItems(), deletedTask)

                val undoSnackbar = Snackbar.make(
                    main_coordinator,
                    R.string.alert_task_deleted,
                    Snackbar.LENGTH_LONG
                )
                undoSnackbar.setAction(R.string.action_undo) {
                    mViewAdapter.onDeleteUndo(deletedTask.order)
                    mViewModel.onTaskDeleteUndo(mViewAdapter.getItems(), deletedTask)
                }
                undoSnackbar.show()
            }
        }
        ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(task_recycler)
    }

    /**
     * Adapted from here:
     *
     * https://stackoverflow.com/a/34740127/10518087
     * https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete/
     */
    private fun setUpAnimationDecoratorHelper() {
        task_recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            private val background = ColorDrawable(
                ContextCompat.getColor(this@TasksActivity, R.color.negativeColor)
            )

            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.itemAnimator?.isRunning == true && mDeleteBgEnabled && !mDragging) {
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
        mViewModel.listsWithTasks.observe(this, Observer { listsWithTasks ->
            // Lists will be in date order, from oldest to newest.

            // The lists menu is inside the main drawer menu.
            val listsMenu = nav_view.menu[1].subMenu
            listsMenu.clear()

            if (listsWithTasks != null) {
                if (listsWithTasks.isNotEmpty()) {
                    var listIsSet = false
                    for (i in listsWithTasks.indices) {
                        // Handle lists.
                        val listWithTasks = listsWithTasks[i]
                        val list = listWithTasks.taskList
                        listsMenu.add(R.id.group_lists, list.id, Menu.NONE, list.name)
                        val counter = layoutInflater.inflate(
                            R.layout.menu_counter,
                            drawer_layout,
                            false
                        )
                        counter.findViewById<TextView>(R.id.text_counter)
                            .text = listWithTasks.tasks.size.toString()
                        listsMenu[i].actionView = counter

                        if (mViewModel.activeList == 0 && i == listsWithTasks.size - 1) {
                            // If active list is set to 0, then the most recent list must be set as
                            // active, in order to activate the newly added list.
                            setTaskList(listWithTasks)
                            listIsSet = true
                        } else if (mViewModel.activeList == -1 ||
                            list.id == mViewModel.activeList
                        ) {
                            // Alternatively, if active list is not set or it is set to this list,
                            // set it as active.
                            setTaskList(listWithTasks)
                            listIsSet = true
                        }
                    }

                    // If list was not set during loop (active list was deleted), set it to the
                    // first list.
                    if (!listIsSet) setTaskList(listsWithTasks[0])
                } else {
                    // No lists; create an empty list with the default name from preferences.
                    val list = TaskList(1, mDefaultListName, MiscUtil.getCurrentTime())
                    mViewModel.insertTaskList(list)
                }

                // Finally set the created nav drawer list group as exclusively checkable.
                listsMenu.setGroupCheckable(R.id.group_lists, true, true)
            }
        })
    }

    /**
     * Save the current active list to shared preferences, so that it will be opened when the user
     * returns.
     */
    private fun writeListPrefs() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit { putInt(Constants.PREF_KEY_ACTIVE_LIST_ID, mViewModel.activeList) }
    }
}
