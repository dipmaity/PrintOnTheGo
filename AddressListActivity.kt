package com.example.plk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plk.adapters.AddressListAdapter
import com.example.plk.databinding.ActivityAddressListBinding
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.Orderitemslist
import com.example.plk.models.Thikana
import com.example.plk.ui.StartActivity
import com.example.plk.utils.Constants
import com.example.plk.utils.SwipeToDeleteCallback
import com.example.plk.utils.SwipeToEditCallback

class AddressListActivity : StartActivity() {
    private lateinit var binding: ActivityAddressListBinding
    // Add a global variable for URI of a selected image from phone storage.
    private var mUserProfileImageURL: String = ""

    private var mOrderDetails: Orderitemslist? = null

    private var mSelectAddress: Boolean = false

    private var orderDetails: Orderitemslist? = null

    private var mModel: Thikana? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)) {
            mSelectAddress =
                intent.getBooleanExtra(Constants.EXTRA_SELECTED_ADDRESS, false)
        }

        if(intent.hasExtra(Constants.EXTRA_MY_ORDER_DETAILS)) {
            mOrderDetails = intent.getParcelableExtra(Constants.EXTRA_MY_ORDER_DETAILS)!!
        }
        setupActionBar(binding.aaToolbar)

        binding.tvAddAddress.setOnClickListener {
            val intent = Intent(this@AddressListActivity, AddEditAddressActivity::class.java)
            startActivityForResult(intent, Constants.ADD_ADDRESS_REQUEST_CODE)
        }

        getAddressList()


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
            if (requestCode == Constants.ADD_ADDRESS_REQUEST_CODE) {
                getAddressList()
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
           Toast.makeText(this@AddressListActivity, "Technical Fault!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * A function to get the list of address from cloud firestore.
     */
    private fun getAddressList() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAddressesList(this@AddressListActivity)
    }

    /**
     * A function to get the success result of address list from cloud firestore.
     *
     * @param addressList
     */
    fun successAddressListFromFirestore(addressList: ArrayList<Thikana>) {

        // Hide the progress dialog
        hideProgressDialog()

        if (addressList.size > 0) {

            binding.rvAddressList.visibility = View.VISIBLE
            binding.tvNoAddressFound.visibility = View.GONE

            binding.rvAddressList.layoutManager = LinearLayoutManager(this@AddressListActivity)
            binding.rvAddressList.setHasFixedSize(true)

            val addressAdapter = AddressListAdapter(this@AddressListActivity, addressList, mSelectAddress)
            binding.rvAddressList.adapter = addressAdapter
            if (!mSelectAddress) {
                val editSwipeHandler = object : SwipeToEditCallback(this) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                        val adapter = binding.rvAddressList.adapter as AddressListAdapter
                        adapter.notifyEditItem(
                            this@AddressListActivity,
                            viewHolder.adapterPosition
                        )
                    }
                }
                val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
                editItemTouchHelper.attachToRecyclerView(binding.rvAddressList)


                val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                        // Show the progress dialog.
                        showProgressDialog(resources.getString(R.string.please_wait))

                        FirestoreClass().deleteAddress(
                            this@AddressListActivity,
                            addressList[viewHolder.adapterPosition].id
                        )
                    }
                }
                val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
                deleteItemTouchHelper.attachToRecyclerView(binding.rvAddressList)
            }

        } else {
            binding.rvAddressList.visibility = View.GONE
            binding.tvNoAddressFound.visibility = View.VISIBLE
        }
    }

    /**
     * A function notify the user that the address is deleted successfully.
     */
    fun deleteAddressSuccess() {

        // Hide progress dialog.
        hideProgressDialog()

        Toast.makeText(
            this@AddressListActivity,
            resources.getString(R.string.err_your_address_deleted_successfully),
            Toast.LENGTH_SHORT
        ).show()

        getAddressList()
    }

    fun placeAnOrder(model : Thikana) {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        mModel= model
        FirestoreClass().uploadImageToCloudStorage(this@AddressListActivity, mOrderDetails!!.file, mOrderDetails!!.path!!)

    }

    fun orderPlacedSuccess() {

        hideProgressDialog()
        Toast.makeText(this@AddressListActivity, "Your oder is placed", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this@AddressListActivity, MainActivity::class.java))
        finish()

    }

    fun imageUploadSuccess(imageURL: String) {

        mUserProfileImageURL = imageURL
        orderDetails = Orderitemslist(
            mOrderDetails!!.customer,
            mOrderDetails!!.shoper,
            mOrderDetails!!.file,
            mOrderDetails!!.path,
            mModel,
            mUserProfileImageURL
        )
        FirestoreClass().placeOrder(this@AddressListActivity, orderDetails!!)

    }
}