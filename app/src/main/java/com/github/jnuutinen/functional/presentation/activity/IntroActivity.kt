package com.github.jnuutinen.functional.presentation.activity

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.jnuutinen.functional.R
import com.github.jnuutinen.functional.util.Constants
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage

class IntroActivity : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showSkipButton(true)
        showStatusBar(false)

        val extras = intent.extras
        val slideToShow = extras?.getString(Constants.EXTRA_NAME_INTRO_SLIDE)
        if (slideToShow == Constants.EXTRA_VALUE_INTRO_DRAG_DROP) {
            val version = applicationContext.packageManager.getPackageInfo(
                applicationContext.packageName,
                0
            ).versionName
            addSlide(
                AppIntroFragment.newInstance(
                    createMoveTaskSlide(
                        resources.getString(R.string.intro_title_whats_new, version),
                        resources.getString(R.string.intro_message_move_task)
                    )
                )
            )
            showPagerIndicator(false)
        } else {
            addSlide(AppIntroFragment.newInstance(createAddTaskSlide()))
            addSlide(AppIntroFragment.newInstance(createEditTaskSlide()))
            addSlide(AppIntroFragment.newInstance(createDeleteTaskSlide()))
            addSlide(AppIntroFragment.newInstance(createMoveTaskSlide()))
            addSlide(AppIntroFragment.newInstance(createManageListsSlide()))
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        finish()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        finish()
    }

    private fun createAddTaskSlide(): SliderPage {
        val sliderPage = SliderPage()
        sliderPage.apply {
            bgColor = ContextCompat.getColor(this@IntroActivity, R.color.circleIndigo300)
            title = resources.getString(R.string.intro_message_add_task)
            imageDrawable = R.drawable.intro_add_task
        }
        return sliderPage
    }

    private fun createDeleteTaskSlide(): SliderPage {
        val sliderPage = SliderPage()
        sliderPage.apply {
            bgColor = ContextCompat.getColor(this@IntroActivity, R.color.circleRed300)
            title = resources.getString(R.string.intro_message_delete_task)
            imageDrawable = R.drawable.intro_delete_task
        }
        return sliderPage
    }

    private fun createEditTaskSlide(): SliderPage {
        val sliderPage = SliderPage()
        sliderPage.apply {
            bgColor = ContextCompat.getColor(this@IntroActivity, R.color.circleTeal300)
            title = resources.getString(R.string.intro_message_edit_task)
            imageDrawable = R.drawable.intro_edit_task
        }
        return sliderPage
    }

    private fun createManageListsSlide(): SliderPage {
        val sliderPage = SliderPage()
        sliderPage.apply {
            bgColor = ContextCompat.getColor(this@IntroActivity, R.color.circleBlueGrey700)
            title = resources.getString(R.string.intro_message_manage_lists)
            imageDrawable = R.drawable.intro_lists
        }
        return sliderPage
    }

    private fun createMoveTaskSlide(
        titleStr: String = resources.getString(R.string.intro_message_move_task),
        descriptionStr: String = ""
    ): SliderPage {
        val sliderPage = SliderPage()
        sliderPage.apply {
            bgColor = ContextCompat.getColor(this@IntroActivity, R.color.circleBrown300)
            title = titleStr
            imageDrawable = R.drawable.intro_move_tasks
            description = descriptionStr
        }
        return sliderPage
    }
}
