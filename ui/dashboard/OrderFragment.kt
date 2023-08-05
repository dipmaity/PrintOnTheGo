package com.example.plk.ui.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.plk.BasekFragment
import com.example.plk.OrderDetailsActivity
import com.example.plk.R
import com.example.plk.adapters.MyOrdersListAdapter
import com.example.plk.adapters.ShopListAdapter
import com.example.plk.databinding.FragmentOrderBinding
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.Orderitemslist
import com.example.plk.models.User
import com.example.plk.uploadActivity
import com.example.plk.utils.Constants

class OrderFragment : BasekFragment() {

    private lateinit var binding: FragmentOrderBinding
    private lateinit var mUserDetails: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       binding = FragmentOrderBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getOrderItemsList()

    }
    fun userDetailsSuccess(user:User) {
        Log.e("print", "third")
        mUserDetails = user
        var check:String = ""
        if(mUserDetails.type == Constants.CUSTOMER) {
            check = "customer"
        }
        else {
            check = "shoper"
        }

        FirestoreClass().getOrderItemsList(this@OrderFragment, mUserDetails, check)
    }

    private fun getOrderItemsList() {
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getUserDetails(this@OrderFragment)


    }

    fun populateOrdersListInUI(ordersList: ArrayList<Orderitemslist>) {

        // Hide the progress dialog.
        hideProgressDialog()

        // TODO Step 11: Populate the orders list in the UI.
        // START
        if (ordersList.size > 0) {

            binding.ofRvMyOrderItems.visibility = View.VISIBLE
            binding.ofTvNoOrdersFound.visibility = View.GONE

            binding.ofRvMyOrderItems.layoutManager = LinearLayoutManager(activity)
            binding.ofRvMyOrderItems.setHasFixedSize(true)

            val myOrdersAdapter = MyOrdersListAdapter(requireActivity(), ordersList, mUserDetails.type)
            binding.ofRvMyOrderItems.adapter = myOrdersAdapter

            myOrdersAdapter.setOnClickListener(object:
                MyOrdersListAdapter.OnClickListener{
                override fun onClick(position: Int, product: Orderitemslist) {
                    FirestoreClass().getOrder(this@OrderFragment,product.id)


                }
            })
        } else {
            binding.ofRvMyOrderItems.visibility = View.GONE
            binding.ofTvNoOrdersFound.visibility = View.VISIBLE
        }
        // END
    }

    fun orderDeleteSuccess() {

        // Hide progress dialog.
        hideProgressDialog()

        Toast.makeText(
            requireActivity(),
            resources.getString(R.string.err_your_order_cancel_successfully),
            Toast.LENGTH_SHORT
        ).show()

        getOrderItemsList()
    }

    fun getOrderDetailsSuccess(order:Orderitemslist) {
        val intent = Intent(context, OrderDetailsActivity::class.java)
        intent.putExtra(Constants.EXTRA_USER_DETAILS, order)
        startActivity(intent)
    }


}