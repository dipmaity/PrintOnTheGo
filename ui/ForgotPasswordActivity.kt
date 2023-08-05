package com.example.plk.ui

import android.os.Bundle
import android.widget.Toast
import com.example.plk.R
import com.example.plk.databinding.ActivityForgotPasswordBinding
import com.example.plk.ui.dashboard.OrderFragment
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : StartActivity() {
    private lateinit var binding :ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
//        setupActionBar(binding.fpaToolbar)
        setContentView(binding.root)
        binding.fpaBtSubmit.setOnClickListener {
            // Get the email id from the input field.
            val email: String = binding.fpaEtEmail.text.toString().trim { it <= ' ' }

            // Now, If the email entered in blank then show the error message or else continue with the implemented feature.
            if (email.isEmpty()) {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
            } else {

                // Show the progress dialog.
                showProgressDialog(resources.getString(R.string.please_wait))

                // This piece of code is used to send the reset password link to the user's email id if the user is registered.
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->

                        // Hide the progress dialog
                        hideProgressDialog()

                        if (task.isSuccessful) {
                            // Show the toast message and finish the forgot password activity to go back to the login screen.
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                resources.getString(R.string.email_sent_success),
                                Toast.LENGTH_LONG
                            ).show()

                            finish()
                        } else {
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    }
            }
        }
    }
}