package com.github.jnuutinen.functional.util

import android.content.Context
import java.util.Calendar

object MiscUtil {

    fun dpFromPx(context: Context, px: Float) = px / context.resources.displayMetrics.density

    fun pxFromDp(context: Context, dp: Float) = dp * context.resources.displayMetrics.density

    fun getCurrentTime() = Calendar.getInstance().time.time
}
