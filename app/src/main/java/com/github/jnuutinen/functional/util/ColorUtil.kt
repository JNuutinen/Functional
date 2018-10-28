package com.github.jnuutinen.functional.util

import com.github.jnuutinen.functional.R
import java.util.*

fun getRandomColor(): Int {
    val colors = listOf(
        R.color.circleRed,
        R.color.circlePink,
        R.color.circlePurple,
        R.color.circleDeepPurple,
        R.color.circleIndigo,
        R.color.circleBlue,
        R.color.circleLightBlue,
        R.color.circleCyan,
        R.color.circleTeal,
        R.color.circleGreen,
        R.color.circleLightGreen,
        R.color.circleLime,
        R.color.circleYellow,
        R.color.circleAmber,
        R.color.circleOrange,
        R.color.circleDeepOrange)
    return colors[(0..colors.size).random()]
}

private fun ClosedRange<Int>.random() = Random().nextInt((endInclusive) - start) + start