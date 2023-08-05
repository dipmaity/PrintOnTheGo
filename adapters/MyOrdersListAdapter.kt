package com.example.plk.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.plk.R
import com.example.plk.models.Orderitemslist
import com.example.plk.models.User
import com.example.plk.utils.Constants
import com.myshoppal.utils.GlideLoader


class MyOrdersListAdapter(
    private val context: Context,
    private var list: ArrayList<Orderitemslist>,
    private var type:String
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // A global variable for OnClickListener interface.
    private var onClickListener: MyOrdersListAdapter.OnClickListener? = null


    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.my_order_list,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(model.download!!) {
            holder.itemView.findViewById<LinearLayout>(R.id.mol_ll_fir).setBackgroundColor(ContextCompat.getColor(context, R.color.statusgreen))
        }

        if (holder is MyViewHolder) {
            if(type == Constants.CUSTOMER) {
                GlideLoader(this.context).loadShopPicture(model.shoper!!.image, holder.itemView.findViewById(R.id.mol_image))
                holder.itemView.findViewById<TextView>(R.id.mol_shop_name).text = model.shoper!!.shop_name
                holder.itemView.findViewById<TextView>(R.id.mol_shoper_name).text = model.shoper.name
                holder.itemView.findViewById<TextView>(R.id.mol_location).text = model.shoper.city
                holder.itemView.findViewById<TextView>(R.id.mol_tv_path).text = model.path
            }
            else {
                GlideLoader(this.context).loadUserPicture(model.customer!!.image, holder.itemView.findViewById(R.id.mol_image))
                holder.itemView.findViewById<TextView>(R.id.mol_shop_name).text = model.customer!!.name
                holder.itemView.findViewById<TextView>(R.id.mol_shoper_name).text = model.address?.mobileNumber
                holder.itemView.findViewById<TextView>(R.id.mol_location).text = model.address?.zipCode
                holder.itemView.findViewById<TextView>(R.id.mol_tv_path).text = model.path

            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                        onClickListener!!.onClick(position, model)
                    }
            }
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    /**
     * A function for OnClickListener where the Interface is the expected parameter and assigned to the global variable.
     *
     * @param onClickListener
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
    /**
     * An interface for onclick items.
     */
    interface OnClickListener {

        fun onClick(position: Int, product: Orderitemslist)
    }
}