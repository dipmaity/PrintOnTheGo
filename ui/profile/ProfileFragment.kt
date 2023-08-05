package com.example.plk.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.plk.AddressListActivity
import com.example.plk.BasekFragment
import com.example.plk.R
import com.example.plk.databinding.FragmentProfileBinding
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.User
import com.example.plk.ui.LoginActivity
import com.example.plk.ui.UserProfileActivity
import com.myshoppal.utils.GlideLoader
import com.example.plk.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : BasekFragment(), View.OnClickListener {
    private lateinit var binding:FragmentProfileBinding
    // A variable for user details which will be initialized later on.
    private lateinit var mUserDetails: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.pfTvShopName.visibility = View.GONE
        binding.pfTvLocation.visibility = View.GONE
        binding.pfLlAddress.visibility = View.GONE

        binding.pfBtnEdit.setOnClickListener(this)
        binding.pfBtnLogout.setOnClickListener(this)
        binding.pfLlAddress.setOnClickListener(this)
        return binding.root

    }

    override fun onResume() {
        super.onResume()
        getUserDetails()
    }

    private fun getUserDetails() {
        // Show the progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        // Call the function of Firestore class to get the user details from firestore which is already created.
        FirestoreClass().getUserDetails(this@ProfileFragment)
    }

    /**
     * A function to receive the user details and populate it in the UI.
     */
    fun userDetailsSuccess(user: User) {

        mUserDetails = user

        // Hide the progress dialog
        hideProgressDialog()

        if(user.type == Constants.CUSTOMER) {
            binding.pfTvShopName.visibility = View.GONE
            binding.pfTvLocation.visibility = View.GONE
            binding.pfLlAddress.visibility = View.VISIBLE
            // Load the image using the Glide Loader class.
            GlideLoader(this.requireContext()).loadUserPicture(user.image, binding.pfIvUserImage)
        }
        else {
            binding.pfTvShopName.visibility = View.VISIBLE
            binding.pfTvLocation.visibility = View.VISIBLE
            binding.pfLlAddress.visibility = View.GONE
            // Load the image using the Glide Loader class.
            GlideLoader(this.requireContext()).loadShopPicture(user.image, binding.pfIvUserImage)
            binding.pfTvShopName.text = user.shop_name
            binding.pfTvLocation.text = user.city
        }


        binding.pfTvName.text = user.name
//        binding.pfTvGender.text = user.gender
        binding.pfTvEmail.text = user.email
        binding.pfTvMobileNumber.text = "+91 ${user.mobile}"
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.pf_btn_edit -> {
                    val intent = Intent(this@ProfileFragment.requireActivity(), UserProfileActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
                    startActivity(intent)
                }

                R.id.pf_ll_address -> {
                    val intent = Intent(this@ProfileFragment.requireActivity(), AddressListActivity::class.java)
                    startActivity(intent)
                }

                R.id.pf_btn_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this@ProfileFragment.requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    onDestroy()
                }
            }
        }
    }

}