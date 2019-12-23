package com.github.jnuutinen.functional.presentation

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TaskItemDivider(color: Int, width: Float) : RecyclerView.ItemDecoration() {
    private val mPaint = Paint()
    private val mAlpha: Int

    init {
        mPaint.color = color
        mPaint.strokeWidth = width
        mAlpha = mPaint.alpha
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val params = view.layoutParams as RecyclerView.LayoutParams
        val position = params.viewAdapterPosition
        if (position < state.itemCount) {
            outRect.set(0, 0, 0, mPaint.strokeWidth.toInt())
        } else {
            outRect.setEmpty()
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)

            val left = child.left + child.translationX
            val top = child.bottom + child.translationY
            val right = child.right + child.translationX

            mPaint.alpha = child.alpha.toInt() * mAlpha
            c.drawLine(left, top, right, top, mPaint)
        }
    }
}
