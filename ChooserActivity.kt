package com.example.plk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.plk.databinding.ActivityChooserBinding
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.User
import com.example.plk.ui.StartActivity
import com.example.plk.ui.UserProfileActivity
import com.example.plk.utils.Constants

class ChooserActivity : StartActivity() {
    private lateinit var binding: ActivityChooserBinding
    private lateinit var user: User
    private lateinit var mUser: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooserBinding.inflate(layoutInflater)
        if(intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            user = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }
        binding.caBtLogin.setOnClickListener {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            val type:String = if(binding.caRbCustomer.isChecked) {
                Constants.CUSTOMER
            }
            else {
                Constants.SHOPER
            }
            mUser = User(user.id, type,user.name, user.email)
            FirestoreClass().registerUser(this@ChooserActivity, mUser)

        }
        setContentView(binding.root)
    }

    fun userRegistrationSuccess() {
        // Hide the progress dialog
        hideProgressDialog()
        Toast.makeText(
            this@ChooserActivity,
            resources.getString(R.string.register_success),
            Toast.LENGTH_SHORT
        ).show()

        val intent: Intent = Intent(this@ChooserActivity, UserProfileActivity::class.java)
        intent.putExtra(Constants.EXTRA_USER_DETAILS, mUser)
        startActivity(intent)
        finish()
    }
}