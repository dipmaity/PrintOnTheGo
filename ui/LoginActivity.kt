package com.example.plk.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.plk.ChooserActivity
import com.example.plk.ExtraSignInActivity
import com.example.plk.MainActivity
import com.example.plk.R
import com.example.plk.databinding.ActivityLoginBinding
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.User
import com.example.plk.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : StartActivity(), View.OnClickListener {
    private lateinit var binding :ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    // For google signin
    val Req_Code: Int = 123
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001 // Request code for Google Sign-In
//    private  var mUser: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // Click event assigned to Forgot Password text.
        binding.laTvForgetPassword.setOnClickListener(this)
        // Click event assigned to Login button.
        binding.laBtLogin.setOnClickListener(this)
        // Click event assigned to Register text.
        binding.laTvRegister.setOnClickListener(this)
        binding.laIvGoogle.setOnClickListener(this)
        binding.laIvFacebook.setOnClickListener(this)
        binding.laIvGithub.setOnClickListener(this)

    }
    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.la_tv_forget_password -> {

                    // Launch the forgot password screen when the user clicks on the forgot password text.
                    val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                }

                R.id.la_bt_login -> {

                    logInRegisteredUser()
                }

                R.id.la_tv_register -> {
                    // Launch the register screen when the user clicks on the text.
                    val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }
                R.id.la_iv_github-> {
                    Toast.makeText(this@LoginActivity, "Coming Soon", Toast.LENGTH_SHORT).show()
                }
                R.id.la_iv_facebook -> {
                    Toast.makeText(this@LoginActivity, "Coming Soon", Toast.LENGTH_SHORT).show()
                }
                R.id.la_iv_google -> {
                    signInGoogle()
                }
            }
        }
    }

    /**
     * A function to validate the login entries of a user.
     */
    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(binding.laEtEmail.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(binding.laEtPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to Log-In. The user will be able to log in using the registered email and password with Firebase Authentication.
     */
    private fun logInRegisteredUser() {

        if (validateLoginDetails()) {

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            // Get the text from editText and trim the space
            val email = binding.laEtEmail.text.toString().trim { it <= ' ' }
            val password = binding.laEtPassword.text.toString().trim { it <= ' ' }

            // Log-In using FirebaseAuth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        FirestoreClass().getUserDetails(this@LoginActivity)
                    } else {
                        // Hide the progress dialog
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    /**
     * A function to notify user that logged in success and get the user details from the FireStore database after authentication.
     */
    fun userLoggedInSuccess(user: User) {

        // Hide the progress dialog.
        hideProgressDialog()
//        mUser = user!!

        if (user.profileCompleted == 0) {
            // If the user profile is incomplete then launch the UserProfileActivity.
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        } else {
            // Redirect the user to Dashboard Screen after log in.
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }

    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Req_Code)
    }

    // onActivityResult() function : this is where
    // we provide the task and data for the Google Account
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)

            if (account != null) {
                auth.fetchSignInMethodsForEmail(account.email!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val result = task.result
                            if (result != null && result.signInMethods != null && result.signInMethods!!.isNotEmpty()) {
                                // The email is authenticated with Google Sign-In
                                showProgressDialog(resources.getString(R.string.please_wait))
                                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Firebase registered user
                                        val firebaseUser: FirebaseUser = task.result!!.user!!
                                        FirestoreClass().getUserDetailsWithId(this@LoginActivity, firebaseUser.uid)
                                    }
                                }
                            } else {
                                // The email is not authenticated with Google Sign-In
                                UpdateUI(account)
                            }
                        } else {
                            // An error occurred while checking the email authentication status
//                            val e = task.exception as? FirebaseAuthException
//                            Log.e(TAG, "Error checking email authentication status: ${e?.errorCode}")
                        }

                    }
            }
        } catch (e: ApiException) {
//            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    // this is where we update the UI after Google signin takes place
    private fun UpdateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Firebase registered user
                val firebaseUser: FirebaseUser = task.result!!.user!!
                val user = User(firebaseUser.uid,
                    "",
                        account.displayName.toString(),
                        account.email.toString()
                    )

                val intent:Intent = Intent(this@LoginActivity, ChooserActivity::class.java)
                intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
                startActivity(intent)
                finish()
            }
        }
    }

}