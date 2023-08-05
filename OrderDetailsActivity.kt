package com.example.plk

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.example.plk.databinding.ActivityOrderDetailsBinding
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.Orderitemslist
import com.example.plk.ui.StartActivity
import com.example.plk.utils.Constants
import com.myshoppal.utils.GlideLoader

class OrderDetailsActivity : StartActivity() {
    private lateinit var binding:ActivityOrderDetailsBinding
    private lateinit var mOrder:Orderitemslist
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setupActionBar(binding.odaToolbar)
        if(intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            mOrder = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }
        var id:String = FirestoreClass().getCurrentUserID()
        if(mOrder.shoper!!.id == id) {
            //  Shoper View
            binding.odaShoperDetails.visibility = View.GONE
            binding.odaCancelLayout.visibility = View.GONE
            GlideLoader(this@OrderDetailsActivity).loadUserPicture(mOrder.customer!!.image, binding.odaImage)
            binding.odaShopName.text = mOrder.customer!!.name
            binding.odaLocation.text = "+91 ${mOrder.customer!!.mobile.toString()}"
        }
        else {
            binding.odaShoperDetails.visibility = View.VISIBLE
            binding.odaCancelLayout.visibility = View.VISIBLE
            GlideLoader(this@OrderDetailsActivity).loadShopPicture(mOrder.shoper!!.image, binding.odaImage)
            binding.odaShopName.text = mOrder.shoper!!.shop_name
            binding.odaLocation.text = mOrder.shoper!!.city
            binding.odaShoperName.text = mOrder.shoper!!.name
            binding.odaShoperNumber.text ="+91  ${mOrder.shoper!!.mobile.toString()}"

        }
        binding.odaDeliverName.text = mOrder.address!!.name
        binding.odaDeliverAddress.text = mOrder.address!!.address
        binding.odaDeliverPincode.text = mOrder.address!!.zipCode
        binding.odaDeliverNumber.text = "+91 ${mOrder.address!!.mobileNumber.toString()}"
        binding.odaFile.text = mOrder.path
        if(mOrder.download!!) {
            binding.odaCancelLayout.visibility = View.GONE
            binding.odaLlStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.statusgreen))
            binding.odaStatus.text = resources.getString(R.string.processing)
        }
        else {
            binding.odaStatus.text = resources.getString(R.string.pending)
        }
        binding.odaDownload.setOnClickListener{
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mOrder.fileURL))
            startActivity(browserIntent)
            mOrder.download = true
            if(mOrder.download!!) {
                binding.odaLlStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.statusgreen))
                binding.odaStatus.text = resources.getString(R.string.processing)
            }
            else {
                binding.odaStatus.text = resources.getString(R.string.pending)
            }
            FirestoreClass().updateOrder(this@OrderDetailsActivity, mOrder)
        }
        binding.odaCancel.setOnClickListener {
            //Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().deleteOrder(this@OrderDetailsActivity,mOrder.id)
        }
        setContentView(binding.root)
    }

    fun orderDeleteSuccess() {
        hideProgressDialog()
        finish()
    }
}