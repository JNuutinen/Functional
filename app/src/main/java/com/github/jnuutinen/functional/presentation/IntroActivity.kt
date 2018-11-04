package com.github.jnuutinen.functional.presentation

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.jnuutinen.functional.R
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage

class IntroActivity : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance(createAddTodoSlide()))
        addSlide(AppIntroFragment.newInstance(createEditTodoSlide()))
        addSlide(AppIntroFragment.newInstance(createDeleteTodoSlide()))
        addSlide(AppIntroFragment.newInstance(createManageListsSlide()))

        showSkipButton(true)
        showStatusBar(false)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        finish()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        finish()
    }

    private fun createAddTodoSlide(): SliderPage {
        val sliderPage = SliderPage()
        sliderPage.apply {
            bgColor = ContextCompat.getColor(this@IntroActivity, R.color.circleIndigo300)
            title = resources.getString(R.string.intro_title_add_todo)
            description = resources.getString(R.string.intro_message_add_todo)
            imageDrawable = R.drawable.intro_add_todo
        }
        return sliderPage
    }

    private fun createDeleteTodoSlide(): SliderPage {
        val sliderPage = SliderPage()
        sliderPage.apply {
            bgColor = ContextCompat.getColor(this@IntroActivity, R.color.circleRed300)
            title = resources.getString(R.string.intro_title_delete_todo)
            description = resources.getString(R.string.intro_message_delete_todo)
            imageDrawable = R.drawable.intro_delete_todo
        }
        return sliderPage
    }

    private fun createEditTodoSlide(): SliderPage {
        val sliderPage = SliderPage()
        sliderPage.apply {
            bgColor = ContextCompat.getColor(this@IntroActivity, R.color.circleTeal300)
            title = resources.getString(R.string.intro_title_edit_todo)
            description = resources.getString(R.string.intro_message_edit_todo)
            imageDrawable = R.drawable.intro_edit_todo
        }
        return sliderPage
    }

    private fun createManageListsSlide(): SliderPage {
        val sliderPage = SliderPage()
        sliderPage.apply {
            bgColor = ContextCompat.getColor(this@IntroActivity, R.color.circleBlueGrey700)
            title = resources.getString(R.string.intro_title_manage_lists)
            description = resources.getString(R.string.intro_message_manage_lists)
            imageDrawable = R.drawable.intro_lists
        }
        return sliderPage
    }
}