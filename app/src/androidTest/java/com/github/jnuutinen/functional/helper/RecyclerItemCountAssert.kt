package com.github.jnuutinen.functional.helper

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.CoreMatchers.`is`

class RecyclerItemCountAssert(private val expectedCount: Int) : ViewAssertion {

    companion object {
        fun hasSize(size: Int) = RecyclerItemCountAssert(size)
    }

    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) throw noViewFoundException

        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter
        assertThat(adapter?.itemCount, `is`(expectedCount))
    }
}
