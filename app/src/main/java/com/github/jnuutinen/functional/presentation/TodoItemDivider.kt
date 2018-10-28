package com.github.jnuutinen.functional.presentation

import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import com.github.jnuutinen.functional.R

class TodoItemDivider(context: Context) : RecyclerView.ItemDecoration() {
    private val mDivider = context.getDrawable(R.drawable.item_divider)!!

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val left = if (child.translationX > 0) child.left + child.translationX.toInt() else child.left - child.translationX.toInt()
            val top = if (child.translationY < 0) child.bottom + params.bottomMargin + child.translationY.toInt() else child.bottom + params.bottomMargin - child.translationY.toInt()
            val right = if (child.translationX > 0) child.right - child.translationX.toInt() else child.right + child.translationX.toInt()
            val bottom = top + mDivider.intrinsicHeight

            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }
}