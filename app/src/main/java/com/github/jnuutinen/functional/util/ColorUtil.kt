package com.github.jnuutinen.functional.util

import com.github.jnuutinen.functional.R
import java.util.Random

object ColorUtil {
    val colors = intArrayOf(
        // R.color.circleRed700,
        // R.color.circlePink700,
        R.color.circleRed300,
        R.color.circlePink300,
        // R.color.circlePurple700,
        // R.color.circleDeepPurple700,
        R.color.circlePurple300,
        R.color.circleDeepPurple300,
        // R.color.circleIndigo700,
        // R.color.circleBlue700,
        R.color.circleIndigo300,
        R.color.circleBlue300,
        // R.color.circleLightBlue700,
        // R.color.circleCyan700,
        R.color.circleLightBlue300,
        R.color.circleCyan300,
        // R.color.circleTeal700,Œ
        // R.color.circleGreen700,
        R.color.circleTeal300,
        R.color.circleGreen300,
        // R.color.circleLightGreen700,
        // R.color.circleLime700,
        R.color.circleLightGreen300,
        R.color.circleLime300,
        // R.color.circleYellow700,
        // R.color.circleAmber700,
        R.color.circleYellow300,
        R.color.circleAmber300,
        // R.color.circleOrange700,
        // R.color.circleDeepOrange700,
        R.color.circleOrange300,
        R.color.circleDeepOrange300,
        // R.color.circleBrown700,
        R.color.circleBrown300,
        // R.color.circleBlueGrey700,
        R.color.circleBlueGrey300)

    fun getRandomColor(): Int {
        return colors[(0..colors.size).random()]
    }

    private fun ClosedRange<Int>.random() = Random().nextInt((endInclusive) - start) + start
}
