package com.workaholic.timetracker.workaholic

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    private var mSignInClient: GoogleSignInClient? = null
    private var mFirebaseAuth: FirebaseAuth? = null

    private var RC_SIGN_IN: Int = 1

    data class Salad (
        val name: String = "",
        val uid: String = ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build()
        mSignInClient = GoogleSignIn.getClient(this, gso)
        mFirebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if( account == null) {
            Log.d("account", "Not signed in")
            signIn()
        } else {
            Log.d("account", "Already signed in " + account.displayName)
            firebaseAuthWithGoogle(account)
        }
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

    private fun signIn() {
        startActivityForResult(mSignInClient?.signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        mSignInClient?.signOut()?.addOnCompleteListener {
            Log.d("account", "Successfully signed out")
            updateUi(null)
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
            updateUi(null)
        }

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("account", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mFirebaseAuth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("account", "signInWithCredential:success")
                        val user = mFirebaseAuth?.currentUser
                        updateUi(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("account", "signInWithCredential:failure", task.exception)
                        updateUi(null)
                    }
                })
    }

    private fun updateUi(account: FirebaseUser?) {
        if(account != null) {
            Log.d("account", "Drawing ui because user signed in")
            Log.d("account", "User " + account.uid)
            FirebaseFirestore.getInstance().collection("workaholic").add(Salad("Tomato", account.uid )).addOnFailureListener {
                Log.d("data", "Failed to push data")
            }.addOnSuccessListener {
                Log.d("data", "Successfully pushed data")
            }
            signOut()
        } else {
            Log.d("account", "Not drawing ui because user is not signed in")
        }
    }
}
