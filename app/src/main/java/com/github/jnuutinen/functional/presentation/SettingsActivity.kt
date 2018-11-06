package com.github.jnuutinen.functional.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.github.jnuutinen.functional.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_layout,
                SettingsFragment()
            )
            .commit()
    }

    companion object {
        class SettingsFragment : PreferenceFragmentCompat() {
            override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
                addPreferencesFromResource(R.xml.preferences)
                // Disallow empty default list name.
                val listNamePreference = preferenceScreen.findPreference(getString(R.string.pref_key_default_list_name))
                listNamePreference.setOnPreferenceChangeListener { _, newValue -> newValue.toString().isNotBlank() }
            }
        }
    }
}
