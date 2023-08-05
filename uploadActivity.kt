package com.example.plk

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.plk.databinding.ActivityUploadBinding
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.Orderitemslist
import com.example.plk.models.User
import com.example.plk.ui.StartActivity
import com.example.plk.utils.Constants
import com.myshoppal.utils.GlideLoader
import java.io.IOException

class uploadActivity : StartActivity(), View.OnClickListener {
    private lateinit var binding: ActivityUploadBinding
    private lateinit var mShoperDetails: User
    private lateinit var mUserDetails:User
    // Add a global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null

    private var path:String = ""

    private lateinit var mOrderDetails: Orderitemslist

    

    // A global variable for product id.
    private var mProductId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            mShoperDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }
        setupActionBar(binding.uaToolbar)
        binding.uaTitlePdf.visibility = View.GONE
        binding.uaBtSubmit.visibility = View.GONE
        binding.uaTitleShop.text = mShoperDetails.shop_name
        binding.uaUploadPdf.setOnClickListener(this)
        binding.uaBtSubmit.setOnClickListener(this)



    }

    override fun onClick(v: View?) {
        if(v != null) {
            when(v.id) {
                R.id.ua_upload_pdf -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        Constants.showFileChooser(this@uploadActivity)
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

                R.id.ua_bt_submit -> {

                    FirestoreClass().getUserDetails(this@uploadActivity)

                }
            }
        }
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
                Constants.showImageChooser(this@uploadActivity)
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
    @SuppressLint("Range")
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = ""
        val contentResolver: ContentResolver = context.contentResolver

        // Query metadata from the given Uri to get the file name
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.let {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
            it.close()
        }

        return fileName
    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
                if (data != null) {
                    try {
                        mSelectedImageFileUri = data.data!!
                        path = getFileName(this, mSelectedImageFileUri!!)
                        GlideLoader(this@uploadActivity).loadUserPicture(
                            R.drawable.ic_vector_file_download_done_24,
                            binding.uaUploadPdf
                        )
                        binding.uaTitlePdf.visibility = View.VISIBLE
                        binding.uaTitlePdf.text = path
                        binding.uaBtSubmit.visibility = View.VISIBLE
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@uploadActivity,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // A log is printed when user close or cancel the image selection.
            Toast.makeText(this@uploadActivity, "File Selection Not Works", Toast.LENGTH_SHORT).show()
        }
    }

    fun userLoggedInSuccess(user: User) {
        mUserDetails = user
        if(mUserDetails.type == Constants.SHOPER) {
            Toast.makeText(this, "Shopkeeper don't allow to upload", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            mOrderDetails = Orderitemslist(
                mUserDetails,
                mShoperDetails,
                mSelectedImageFileUri,
                path
            )
            val intent = Intent(this@uploadActivity, AddressListActivity::class.java)
            intent.putExtra(Constants.EXTRA_SELECTED_ADDRESS, true)
            intent.putExtra(Constants.EXTRA_MY_ORDER_DETAILS, mOrderDetails)
            startActivity(intent)
        }

    }
}