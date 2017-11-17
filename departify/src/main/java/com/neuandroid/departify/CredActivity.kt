package com.neuandroid.departify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

/**
 * Created by Jienan on 2017/11/17.
 */
class CredActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cred)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cred, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> {
                AlertDialog.Builder(this)
                        .setMessage(getText(R.string.cred_declare))
                        .setPositiveButton(R.string.register, { _, _ ->
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://developer.deeparteffects.com/user/registration"))
                            startActivity(browserIntent)
                        })
                        .setNegativeButton(R.string.dismiss, { dialog, _ ->
                            dialog?.dismiss()
                        }).show()
            }
            android.R.id.home -> {
                finish()
            }
        }
        return true
    }
}