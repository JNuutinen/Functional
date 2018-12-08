package com.github.jnuutinen.functional.helper

import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class RecyclerViewMatcher(private val viewId: Int) {

    companion object {
        fun withRecyclerView(viewId: Int) = RecyclerViewMatcher(viewId)
    }

    fun atPosition(position: Int) = atPositionOnView(position, -1)

    private fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {
        return object: TypeSafeMatcher<View>() {
            var resources: Resources? = null
            var childView: View? = null

            override fun describeTo(description: Description?) {
                var idDescription = viewId.toString()
                val res = resources
                if (res != null) {
                    idDescription = try {
                        res.getResourceName(viewId)
                    } catch (ex: Resources.NotFoundException) {
                        "$viewId (resource name not found)"
                    }
                }
                description?.appendText("RecyclerView with id: $idDescription at position $position")
            }

            override fun matchesSafely(item: View): Boolean {
                resources = item.resources
                if (childView == null) {
                    val recyclerView = item.rootView.findViewById<RecyclerView>(viewId)
                    if (recyclerView != null && recyclerView.id == viewId) {
                        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                        if (viewHolder != null) {
                            childView = viewHolder.itemView
                        }
                    } else {
                        return false
                    }
                }

                return if (targetViewId == -1) {
                    item == childView
                } else {
                    val targetView = childView?.findViewById<View>(targetViewId)
                    item == targetView
                }
            }
        }
    }
}