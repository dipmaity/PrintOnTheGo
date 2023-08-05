package com.example.plk.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
object Constants {
    const val EXTRA_SIGN_IN_OPTION :String = "extra_sign_in"
    const val CUSTOMER: String = "customer"
    const val SHOPER:String = "shoper"
    const val MYAPP_PREFERENCES: String = "MyAppPrefs"
    // For Users
    const val USERS: String = "users"

    const val LOGGED_IN_USERNAME: String = "logged_in_username"


    //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult in the Base Activity.
    const val READ_STORAGE_PERMISSION_CODE = 2

    // A unique code of image selection from Phone Storage.
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val ADD_ADDRESS_REQUEST_CODE: Int = 121

    const val USER_PROFILE_IMAGE: String = "User_Profile_Image"
    const val USER_FILE: String = "User_File"

    // Firebase database field names
    const val MOBILE: String = "mobile"
    const val GENDER: String = "gender"
    const val IMAGE: String = "image"
    const val COMPLETE_PROFILE: String = "profileCompleted"
    const val NAME: String = "name"
    const val SHOP_NAME:String = "shop_name"
    const val CITY:String = "city"
    const val USER_ID: String = "user_id"
    // Constant variables for Gender
    const val MALE: String = "Male"
    const val FEMALE: String = "Female"

    const val ADDRESSES: String = "addresses"


    // Intent extra constants.
    const val EXTRA_USER_DETAILS: String = "extra_user_details"
    const val EXTRA_PRODUCT_ID: String = "extra_product_id"
    const val EXTRA_PRODUCT_OWNER_ID: String = "extra_product_owner_id"
    const val EXTRA_ADDRESS_DETAILS: String = "AddressDetails"
    const val EXTRA_SELECT_ADDRESS: String = "extra_select_address"
    const val EXTRA_SELECTED_ADDRESS: String = "extra_selected_address"
    const val EXTRA_MY_ORDER_DETAILS: String = "extra_my_order_details"
    const val EXTRA_SOLD_PRODUCT_DETAILS: String = "extra_sold_product_details"
    const val HOME: String = "Home"
    const val OFFICE: String = "Office"
    const val OTHER: String = "Other"
    const val ORDERS: String = "orders"

    /**
     * A function for user profile image selection from phone storage.
     */
    fun showImageChooser(activity: Activity) {
        // An intent for launching the image selection of phone storage.
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Launches the image selection of phone storage using the constant code.
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun showFileChooser(activity: Activity) {

        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)

        activity.startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_IMAGE_REQUEST_CODE)
    }

    /**
     * A function to get the image file extension of the selected image.
     *
     * @param activity Activity reference.
     * @param uri Image file uri.
     */
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}