package com.example.plk.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.plk.ChooserActivity
import com.example.plk.MainActivity
import com.example.plk.R
import com.example.plk.databinding.ActivityRegisterBinding
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.User
import com.example.plk.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class RegisterActivity : StartActivity(), View.OnClickListener {
    private lateinit var binding: ActivityRegisterBinding

    // For google signin

    private val auth = FirebaseAuth.getInstance()
    val Req_Code: Int = 123
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001 // Request code for Google Sign-In

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // This is used to hide the status bar and make the splash screen as a full screen activity.
        // It is deprecated in the API level 30. I will update you with the alternate solution soon.
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )
        setupActionBar(binding.raToolbar)

        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.raTilShopName.visibility = View.GONE
        binding.raTilCity.visibility = View.GONE
        binding.raBtRegister.setOnClickListener(this)
        binding.raTvLogin.setOnClickListener(this)
        binding.raRbCustomer.setOnClickListener(this)
        binding.raRbShoper.setOnClickListener(this)
        binding.raIvGoogle.setOnClickListener(this)
        binding.raIvFacebook.setOnClickListener(this)
        binding.raIvGithub.setOnClickListener(this)

    }

    /**
     * A function to validate the entries of a new user.
     */
    private fun validateRegisterDetails(type:String): Boolean {
        if(type == Constants.CUSTOMER) {
            return when {
                TextUtils.isEmpty(binding.raEtName.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_name), true)
                    false
                }

                TextUtils.isEmpty(binding.raEtEmail.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                    false
                }

                TextUtils.isEmpty(binding.raEtPassword.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                    false
                }

                TextUtils.isEmpty(binding.raEtRePassword.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(
                        resources.getString(R.string.err_msg_enter_confirm_password),
                        true
                    )
                    false
                }

                binding.raEtPassword.text.toString()
                    .trim { it <= ' ' } != binding.raEtRePassword.text.toString()
                    .trim { it <= ' ' } -> {
                    showErrorSnackBar(
                        resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),
                        true
                    )
                    false
                }
                !binding.cbTermsAndCondition.isChecked -> {
                    showErrorSnackBar(
                        resources.getString(R.string.err_msg_agree_terms_and_condition),
                        true
                    )
                    false
                }
                else -> {
                    true
                }
            }
        }
        else {
            return when {

                TextUtils.isEmpty(binding.raEtShopName.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_shop_name), true)
                    false
                }

                TextUtils.isEmpty(binding.raEtName.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_shoper_name), true)
                    false
                }
                TextUtils.isEmpty(binding.raEtEmail.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                    false
                }

                TextUtils.isEmpty(binding.raEtCity.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_city), true)
                    false
                }

                TextUtils.isEmpty(binding.raEtPassword.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                    false
                }

                TextUtils.isEmpty(binding.raEtRePassword.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(
                        resources.getString(R.string.err_msg_enter_confirm_password),
                        true
                    )
                    false
                }

                binding.raEtPassword.text.toString()
                    .trim { it <= ' ' } != binding.raEtRePassword.text.toString()
                    .trim { it <= ' ' } -> {
                    showErrorSnackBar(
                        resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),
                        true
                    )
                    false
                }
                !binding.cbTermsAndCondition.isChecked -> {
                    showErrorSnackBar(
                        resources.getString(R.string.err_msg_agree_terms_and_condition),
                        true
                    )
                    false
                }
                else -> {
                     true
                }
            }
        }
    }

    /**
     * A function to register the user with email and password using FirebaseAuth.
     */
    private fun registerUser() {
        val type = if(binding.raRbCustomer.isChecked) {
            Constants.CUSTOMER
        }
        else {
            Constants.SHOPER
        }
        // Check with validate function if the entries are valid or not.
        if (validateRegisterDetails(type)) {

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            val email: String = binding.raEtEmail.text.toString().trim { it <= ' ' }
            val password: String = binding.raEtPassword.text.toString().trim { it <= ' ' }


            // Create an instance and create a register a user with email and password.
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->
                        // If the registration is successfully done
                        if (task.isSuccessful) {
                            // Firebase registered user
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            var shop_name:String = ""
                            var city:String = ""
                            if(type == Constants.SHOPER) {
                                shop_name = binding.raEtShopName.text.toString().trim { it <= ' ' }
                                city = binding.raEtCity.text.toString().trim { it <= ' ' }
                            }
                            // Instance of User data model class.
                            val user = User(
                                firebaseUser.uid,
                                type,
                                binding.raEtName.text.toString().trim { it <= ' ' },
                                email,
                                shop_name,
                                city
                            )
                            // Pass the required values in the constructor.
                            FirestoreClass().registerUser(this@RegisterActivity, user)
                        } else {
                            // Hide the progress dialog
                            hideProgressDialog()
                            // If the registering is not successful then show error message.
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    })
        }
    }

    /**
     * A function to notify the success result of Firestore entry when the user is registered successfully.
     */
    fun userRegistrationSuccess() {

        // Hide the progress dialog
        hideProgressDialog()

        Toast.makeText(
            this@RegisterActivity,
            resources.getString(R.string.register_success),
            Toast.LENGTH_SHORT
        ).show()


        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        FirebaseAuth.getInstance().signOut()
        // Finish the Register Screen
        finish()
    }

    override fun onClick(v: View?) {
        if(v != null) {
            when(v.id) {
                R.id.ra_bt_register -> {
                    registerUser()
                }

                R.id.ra_tv_login -> {
                    // Here when the user click on login text we can either call the login activity or call the onBackPressed function.
                    // We will call the onBackPressed function.
                    onBackPressed()
                }

                R.id.ra_rb_shoper -> {
                    binding.raTilName.hint = resources.getString(R.string.shoper_name)
                    binding.raTilShopName.visibility = View.VISIBLE
                    binding.raTilCity.visibility = View.VISIBLE
                }

                R.id.ra_rb_customer -> {
                    binding.raTilName.hint = resources.getString(R.string.name)
                    binding.raTilShopName.visibility = View.GONE
                    binding.raTilCity.visibility = View.GONE
                }

                R.id.ra_iv_google -> {
                    signInGoogle()
                }

                R.id.ra_iv_facebook -> {
                    Toast.makeText(this@RegisterActivity, "Coming Soon", Toast.LENGTH_SHORT).show()
                }

                R.id.ra_iv_github -> {
                    Toast.makeText(this@RegisterActivity, "Coming Soon", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                                        FirestoreClass().getUserDetailsWithId(this@RegisterActivity, firebaseUser.uid)
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

                val intent:Intent = Intent(this@RegisterActivity, ChooserActivity::class.java)
                intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
                startActivity(intent)
                finish()
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
            val intent = Intent(this@RegisterActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        } else {
            // Redirect the user to Dashboard Screen after log in.
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }

}