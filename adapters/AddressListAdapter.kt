package com.example.plk.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plk.AddEditAddressActivity
import com.example.plk.AddressListActivity
import com.example.plk.R
import com.example.plk.models.Thikana
import com.example.plk.utils.Constants

class AddressListAdapter(
    private val context: AddressListActivity,
    private var list: ArrayList<Thikana>,
    private val selectAddress: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_address_layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.findViewById<TextView>(R.id.ial_tv_address_full_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.ial_tv_address_type).text = model.type
            holder.itemView.findViewById<TextView>(R.id.ial_tv_address_details).text = model.address
            holder.itemView.findViewById<TextView>(R.id.ial_tv_address_mobile_number).text = model.mobileNumber

            if (selectAddress) {
                holder.itemView.setOnClickListener {
                    context.placeAnOrder(model)
                }
            }
        }
    }

    /**
     * A function to edit the address details and pass the existing details through intent.
     *
     * @param activity
     * @param position
     */
    fun notifyEditItem(activity: Activity, position: Int) {
        val intent = Intent(context, AddEditAddressActivity::class.java)
        intent.putExtra(Constants.EXTRA_ADDRESS_DETAILS, list[position])
        activity.startActivityForResult(intent, Constants.ADD_ADDRESS_REQUEST_CODE)
        notifyItemChanged(position) // Notify any registered observers that the item at position has changed.
    }


    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)


}