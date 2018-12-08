package com.github.jnuutinen.functional.ui

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.helper.RecyclerViewMatcher
import com.github.jnuutinen.functional.helper.TestDatabaseHelper.Companion.repopulateDb
import com.github.jnuutinen.functional.helper.TestDatabaseHelper.Companion.setActiveListSharedPref
import com.github.jnuutinen.functional.presentation.activity.TodosActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class EditTodoTest {

    @Rule
    @JvmField
    val mActivityTestRule = object : ActivityTestRule<TodosActivity>(
        TodosActivity::class.java, false, false) {
        override fun beforeActivityLaunched() {
            setActiveListSharedPref(InstrumentationRegistry.getInstrumentation().targetContext, 2)
            super.beforeActivityLaunched()
        }
    }

    @Before
    fun setUp() {
        repopulateDb()
        mActivityTestRule.launchActivity(null)
        Thread.sleep(1000)
    }

    @Test
    fun editTodo() {
        // Edit the second to-do.
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(1))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.edit_todo_add))
            .perform(ViewActions.replaceText("Edited to-do"), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withText("SAVE"))
            .perform(ViewActions.click())

        // The second to-do should be updated.
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("Edited to-do"))))
    }
}