package com.workaholic.timetracker.workaholic

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private var user: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        if(user == null) {
            val intent = Intent(this, AuthActivity::class.java)
            startActivityForResult(intent, AuthActivity.SIGN_IN)
        } else {
            updateUI()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == AuthActivity.SIGN_IN) {
            if(resultCode == AuthActivity.RC_SUCCESS) {
                user = data!!.getStringExtra("user")!!
            }
            updateUI()
        }
    }

    private fun updateUI() {
        Log.d("account", "Updating ui " + (user ?: "null"))
    }

}
