package com.workaholic.timetracker.workaholic

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

private fun buildSignInOptions(activity: AppCompatActivity) =
        GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .build()

class AuthActivity : AppCompatActivity() {
    private data class Data (
        private var activity: AppCompatActivity,
        val signInOptions: GoogleSignInOptions = buildSignInOptions(activity),
        val signInClient: GoogleSignInClient = GoogleSignIn.getClient(activity, signInOptions),
        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    )

    private var data: Data? = null

    companion object {
        private const val RC_SIGN_IN: Int = 1
        const val RC_SUCCESS: Int = 2
        const val RC_FAILURE: Int = 3
        const val SIGN_IN: Int = 4
        const val RESULT_ACTION: String = "com.workaholic.timetracker.AUTH_RESULT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        data = Data(this)
    }

    override fun onStart() {
        super.onStart()
        GoogleSignIn.getLastSignedInAccount(this)
                ?.let { firebaseAuthWithGoogle(it) }
                ?: googleSignIn()
    }

    private fun googleSignIn() {
        startActivityForResult(data!!.signInClient.signInIntent, RC_SIGN_IN)
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("account", "signInResult:failed code=" + e.statusCode)
            done(null)
        }

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("account", "firebaseAuthWithGoogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        data!!.firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("account", "signInWithCredential:success")
                        val user = data!!.firebaseAuth.currentUser
                        done(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("account", "signInWithCredential:failure", task.exception)
                        done(null)
                    }
                })
    }
    private fun done(account: FirebaseUser?) {
        if(account != null) {
            val intent = Intent(RESULT_ACTION).apply {
                putExtra("user", account.uid)
            }
            setResult(RC_SUCCESS, intent)
            finish()
        } else {
            setResult(RC_FAILURE)
            finish()
        }
    }
}
