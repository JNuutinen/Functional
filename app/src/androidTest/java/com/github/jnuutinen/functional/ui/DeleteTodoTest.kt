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
import com.github.jnuutinen.functional.helper.RecyclerItemCountAssert
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
class DeleteTodoTest {

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
    fun deleteTodo_First() {
        // Ensure the first and second to-dos are correct.
        val firstTodoName = "First list, first to-do"
        val secondTodoName = "First list, second to-do"
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(0))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(firstTodoName))))
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(secondTodoName))))

        // Delete the first to-do by swiping it.
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(0))
            .perform(ViewActions.swipeRight())

        // The former second to-do should now be the first one.
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(0))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(secondTodoName))))

        // The number of to-dos should now be 2.
        Espresso.onView(ViewMatchers.withId(R.id.todo_recycler)).check(RecyclerItemCountAssert.hasSize(2))
    }

    @Test
    fun deleteTodo_Middle() {
        // Ensure the second (middle) and last (third) to-dos are correct.
        val secondTodoName = "First list, second to-do"
        val thirdTodoName = "First list, third to-do"
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(secondTodoName))))
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(2))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(thirdTodoName))))

        // Delete the second to-do by swiping it.
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(1))
            .perform(ViewActions.swipeLeft())

        // The former third to-do should now be the second one.
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(thirdTodoName))))

        // The number of to-dos should now be 2.
        Espresso.onView(ViewMatchers.withId(R.id.todo_recycler)).check(RecyclerItemCountAssert.hasSize(2))
    }

    @Test
    fun deleteTodo_Last() {
        // Ensure the second (middle) and last (third) to-dos are correct.
        val secondTodoName = "First list, second to-do"
        val thirdTodoName = "First list, third to-do"
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(secondTodoName))))
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(2))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(thirdTodoName))))

        // Delete the third to-do by swiping it.
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(2))
            .perform(ViewActions.swipeLeft())

        // The former second to-do should still be the second one.
        Espresso.onView(RecyclerViewMatcher.withRecyclerView(R.id.todo_recycler).atPosition(1))
            .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText(secondTodoName))))

        // The number of to-dos should now be 2.
        Espresso.onView(ViewMatchers.withId(R.id.todo_recycler)).check(RecyclerItemCountAssert.hasSize(2))
    }
}