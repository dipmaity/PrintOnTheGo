package com.example.plk.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plk.BasekFragment
import com.example.plk.R
import com.example.plk.adapters.ShopListAdapter
import com.example.plk.databinding.FragmentShopBinding
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.User
import com.example.plk.uploadActivity
import com.example.plk.utils.Constants

class ShopFragment : BasekFragment() {

    private lateinit var binding: FragmentShopBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        getDashboardItemsList()
    }

    /**
     * A function to get the dashboard items list from cloud firestore.
     */
    private fun getDashboardItemsList() {
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getDashboardItemsList(this@ShopFragment)
    }

    /**
     * A function to get the success result of the dashboard items from cloud firestore.
     *
     * @param dashboardItemsList
     */
    fun successDashboardItemsList(dashboardItemsList: ArrayList<User>) {

        // Hide the progress dialog.
        hideProgressDialog()

        if (dashboardItemsList.size > 0) {

            binding.rvDashboardItems.visibility = View.VISIBLE
            binding.tvNoDashboardItemsFound.visibility = View.GONE

            binding.rvDashboardItems.layoutManager = LinearLayoutManager(activity)
            binding.rvDashboardItems.setHasFixedSize(true)
            var id:String = FirestoreClass().getCurrentUserID()
            val adapter = ShopListAdapter(requireActivity(), dashboardItemsList)
            binding.rvDashboardItems.adapter = adapter

            adapter.setOnClickListener(object:
            ShopListAdapter.OnClickListener{
                override fun onClick(position: Int, product: User) {
                    val intent = Intent(context, uploadActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, product)
                    startActivity(intent)
                }
            })
        } else {
            binding.rvDashboardItems.visibility = View.GONE
            binding.tvNoDashboardItemsFound.visibility = View.VISIBLE
        }
    }
}