package com.example.plk.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.plk.MainActivity
import com.example.plk.R
import com.example.plk.databinding.ActivityUserProfileBinding
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.User
import com.example.plk.utils.Constants
import com.google.android.gms.common.api.ApiException
import com.myshoppal.utils.GlideLoader
import java.io.IOException

class UserProfileActivity : StartActivity(), View.OnClickListener {
    private lateinit var binding:ActivityUserProfileBinding
    // Instance of User data model class. We will initialize it later on.
    private lateinit var mUserDetails: User

    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null

    private var mUserProfileImageURL: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupActionBar(binding.upaToolbar)
        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            // Get the user details from intent as a ParcelableExtra.
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }
//        if(intent.hasExtra(Constants.EXTRA_SIGN_IN_OPTION)) {
//            var accout: ApiException = intent.getParcelableExtra(Constants.EXTRA_SIGN_IN_OPTION)!!
//            binding.upRgGender.visibility = View.VISIBLE
//
//        }

        // If the profile is incomplete then user is from login screen and wants to complete the profile.
        if (mUserDetails.profileCompleted == 0) {
            // Update the title of the screen to complete profile.
            binding.upaTvTitle.text = resources.getString(R.string.title_complete_profile)
            // Here, the some of the edittext components are disabled because it is added at a time of Registration.
            if(mUserDetails.type == Constants.CUSTOMER) {
                binding.upaShopDeatils.visibility = View.GONE
                binding.upaEtShopName.visibility = View.GONE
                binding.upaEtCity.visibility = View.GONE
                binding.upaEtName.isEnabled = true
                binding.upaEtName.setText(mUserDetails.name)
                binding.upaEtEmail.isEnabled = false
                binding.upaEtEmail.setText(mUserDetails.email)
            }
            else {
                binding.upaShopDeatils.visibility = View.VISIBLE
                binding.upaEtShopName.visibility = View.VISIBLE
                binding.upaEtCity.visibility = View.VISIBLE
                binding.upaTilName.hint = resources.getString(R.string.shoper_name)
                binding.upaEtShopName.isEnabled = true
                if(mUserDetails.shop_name != "")
                    binding.upaEtShopName.setText(mUserDetails.shop_name)
                binding.upaEtName.isEnabled = true
                binding.upaEtName.setText(mUserDetails.name)
                binding.upaEtEmail.isEnabled = false
                binding.upaEtEmail.setText(mUserDetails.email)
                binding.upaEtCity.isEnabled = true
                if(mUserDetails.city != "")
                    binding.upaEtCity.setText(mUserDetails.city)
            }
        } else {

            // Call the setup action bar function.
            setupActionBar(binding.upaToolbar)
            // Update the title of the screen to edit profile.
            binding.upaTvTitle.text = resources.getString(R.string.title_edit_profile)
            if(mUserDetails.type == Constants.CUSTOMER) {
                binding.upaShopDeatils.visibility = View.GONE
                binding.upaEtShopName.visibility = View.GONE
                binding.upaEtCity.visibility = View.GONE
                // Load the image using the GlideLoader class with the use of Glide Library.
                GlideLoader(this@UserProfileActivity).loadUserPicture(mUserDetails.image, binding.upaIvUserImage)
            }
            else {
                binding.upaShopDeatils.visibility = View.VISIBLE
                binding.upaEtShopName.visibility = View.VISIBLE
                binding.upaEtCity.visibility = View.VISIBLE
                binding.upaTilName.hint = R.string.shoper_name.toString()
                binding.upaEtShopName.setText(mUserDetails.shop_name)
                binding.upaEtCity.setText(mUserDetails.city)
                // Load the image using the GlideLoader class with the use of Glide Library.
                GlideLoader(this@UserProfileActivity).loadShopPicture(mUserDetails.image, binding.upaIvUserImage)
            }

            // Set the existing values to the UI and allow user to edit except the Email ID.
            binding.upaEtName.setText(mUserDetails.name)

            binding.upaEtEmail.isEnabled = false
            binding.upaEtEmail.setText(mUserDetails.email)

            if (mUserDetails.mobile != 0L) {
                binding.upaEtMobile.setText(mUserDetails.mobile.toString())
            }
//            if (mUserDetails.gender == Constants.MALE) {
//                binding.upaRbMale.isChecked = true
//            } else {
//                binding.upaRbFemale.isChecked = true
//            }
        }

        // Assign the on click event to the user profile photo.
        binding.upaIvUserImage.setOnClickListener(this@UserProfileActivity)
        // Assign the on click event to the SAVE button.
        binding.upaBtSubmit.setOnClickListener(this@UserProfileActivity)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.upa_iv_user_image -> {

                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        Constants.showImageChooser(this@UserProfileActivity)
                    } else {
                        /*Requests permissions to be granted to this application. These permissions
                         must be requested in your manifest, they should not be granted to your app,
                         and they should have protection level*/
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }

                R.id.upa_bt_submit -> {

                    if (validateUserProfileDetails(mUserDetails.type)) {

                        // Show the progress dialog.
                        showProgressDialog(resources.getString(R.string.please_wait))

                        if (mSelectedImageFileUri != null) {

                            FirestoreClass().uploadImageToCloudStorage(
                                this@UserProfileActivity,
                                mSelectedImageFileUri,
                                Constants.USER_PROFILE_IMAGE
                            )
                        } else {
                            updateUserProfileDetails()
                        }
                    }
                }
            }
        }
    }

    /**
     * A function to validate the input entries for profile details.
     */
    private fun validateUserProfileDetails(type:String): Boolean {
        if(type == Constants.CUSTOMER) {
            return when {
                TextUtils.isEmpty(binding.upaEtName.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_name), true)
                    false
                }

                TextUtils.isEmpty(binding.upaEtMobile.text.toString().trim { it <= ' ' })-> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number),true)
                    false
                }
                else ->
                true
            }
        }
        else {
            return when {
                TextUtils.isEmpty(binding.upaEtShopName.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_shop_name), true)
                    false
                }

                TextUtils.isEmpty(binding.upaEtName.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_shoper_name), true)
                    false
                }

                TextUtils.isEmpty(binding.upaEtCity.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_city), true)
                    false
                }
                TextUtils.isEmpty(binding.upaEtMobile.text.toString().trim { it <= ' ' }) -> {
                    showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number),true )
                    false
                }
                else ->
                    true
            }
        }
    }

    /**
     * A function to update user profile details to the firestore.
     */
    private fun updateUserProfileDetails() {

        val userHashMap = HashMap<String, Any>()

        // Get the Name from editText and trim the space
        if(mUserDetails.type == Constants.SHOPER) {
            val shop_name = binding.upaEtShopName.text.toString().trim(){it <= ' '}
            if(shop_name != mUserDetails.shop_name) {
                userHashMap[Constants.SHOP_NAME] = shop_name
            }
            val city = binding.upaEtCity.text.toString().trim(){it <= ' '}
            if(city != mUserDetails.city) {
                userHashMap[Constants.CITY] = city
            }
        }
        val Name = binding.upaEtName.text.toString().trim { it <= ' ' }
        if (Name != mUserDetails.name) {
            userHashMap[Constants.NAME] = Name
        }

        // Here we get the text from editText and trim the space
        val mobileNumber = binding.upaEtMobile.text.toString().trim { it <= ' ' }
//        val gender = if (binding.upaRbMale.isChecked) {
//            Constants.MALE
//        } else {
//            Constants.FEMALE
//        }

        if (mUserProfileImageURL.isNotEmpty()) {
            userHashMap[Constants.IMAGE] = mUserProfileImageURL
        }

        if (mobileNumber.isNotEmpty() && mobileNumber != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }

//        if (gender.isNotEmpty() && gender != mUserDetails.gender) {
//            userHashMap[Constants.GENDER] = gender
//        }

        // Here if user is about to complete the profile then update the field or else no need.
        // 0: User profile is incomplete.
        // 1: User profile is completed.
        if (mUserDetails.profileCompleted == 0) {
            userHashMap[Constants.COMPLETE_PROFILE] = 1
        }

        // call the registerUser function of FireStore class to make an entry in the database.
        FirestoreClass().updateUserProfileData(
            this@UserProfileActivity,
            userHashMap
        )
    }

    /**
     * A function to notify the success result of image upload to the Cloud Storage.
     *
     * @param imageURL After successful upload the Firebase Cloud returns the URL.
     */
    fun imageUploadSuccess(imageURL: String) {

        mUserProfileImageURL = imageURL

        updateUserProfileDetails()
    }

    /**
     * A function to notify the success result and proceed further accordingly after updating the user details.
     */
    fun userProfileUpdateSuccess() {

        // Hide the progress dialog
        hideProgressDialog()

        Toast.makeText(
            this@UserProfileActivity,
            resources.getString(R.string.msg_profile_update_success),
            Toast.LENGTH_SHORT
        ).show()


        // Redirect to the Main Screen after profile completion.
        startActivity(Intent(this@UserProfileActivity, MainActivity::class.java))
        finish()
    }

    /**
     * This function will identify the result of runtime permission after the user allows or deny permission based on the unique code.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@UserProfileActivity)
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
                if (data != null) {
                    try {

                        // The uri of selected image from phone storage.
                        mSelectedImageFileUri = data.data!!

                        GlideLoader(this@UserProfileActivity).loadUserPicture(
                            mSelectedImageFileUri!!,
                            binding.upaIvUserImage
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@UserProfileActivity,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // A log is printed when user close or cancel the image selection.
            Toast.makeText(this@UserProfileActivity, "Image Selection Not Works", Toast.LENGTH_SHORT).show()
        }
    }

}