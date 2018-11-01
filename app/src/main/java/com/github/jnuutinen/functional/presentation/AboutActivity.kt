package com.github.jnuutinen.functional.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.jnuutinen.functional.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val version = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName
        text_app_version.text = getString(R.string.text_app_version, version)

        button_licences.setOnClickListener { startActivity(Intent(this, OssLicensesMenuActivity::class.java)) }
    }
}
