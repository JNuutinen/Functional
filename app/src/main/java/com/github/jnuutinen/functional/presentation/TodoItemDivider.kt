package com.github.jnuutinen.functional.presentation

import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import com.github.jnuutinen.functional.R

class TodoItemDivider(context: Context) : RecyclerView.ItemDecoration() {
    private val mDivider = context.getDrawable(R.drawable.item_divider)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)

            val left = child.left + child.translationX.toInt()
            val top = child.bottom + child.translationY.toInt()
            val right = child.right + child.translationX.toInt()
            val bottom = top + (mDivider?.intrinsicHeight ?: 0)

            mDivider?.setBounds(left, top, right, bottom)

            // This makes sure that the divider is not drawn immediately, when a deleted item is brought back with undo.
            // If alpha is not 1 and divider is drawn, it will be drawn over the possible other moving items.
            if (child.alpha == 1f) mDivider?.draw(c)
        }
    }
}
