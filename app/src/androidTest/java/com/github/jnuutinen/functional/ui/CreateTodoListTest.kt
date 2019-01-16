package com.github.jnuutinen.functional.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.helper.TestDatabaseHelper.Companion.repopulateDb
import com.github.jnuutinen.functional.helper.TestDatabaseHelper.Companion.setActiveListSharedPref
import com.github.jnuutinen.functional.helper.UiTestHelper.Companion.openNavigationDrawer
import com.github.jnuutinen.functional.presentation.activity.TodosActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateTodoListTest {

    @Rule
    @JvmField
    val mActivityTestRule = object : ActivityTestRule<TodosActivity>(
        TodosActivity::class.java, false, false
    ) {
        override fun beforeActivityLaunched() {
            setActiveListSharedPref(InstrumentationRegistry.getInstrumentation().targetContext, 2)
            super.beforeActivityLaunched()
        }
    }

    @Before
    fun setUp() {
        repopulateDb()
        Thread.sleep(1000)
        mActivityTestRule.launchActivity(null)
        Thread.sleep(1000)
    }

    @Test
    fun createList() {
        // Create a new to-do list.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withText("Create a to-do list"))
            .perform(click())
        onView(withId(R.id.edit_list_add))
            .perform(replaceText("A new to-do list"), closeSoftKeyboard())
        onView(withText("CREATE"))
            .perform(click())

        // The created to-do list should be active (toolbar title should have changed).
        onView(withId(R.id.toolbar))
            .check(matches(hasDescendant(withText("A new to-do list"))))

        // The created to-do list should exist in the Navigation Drawer.
        openNavigationDrawer(mActivityTestRule.activity)
        onView(withId(R.id.nav_view))
            .check(matches(hasDescendant(withText("A new to-do list"))))
    }
}