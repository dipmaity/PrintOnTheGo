package com.example.plk.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.plk.R
import com.example.plk.firestore.FirestoreClass
import com.example.plk.models.User
import com.example.plk.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.myshoppal.utils.GlideLoader

open class ShopListAdapter(
    private val context: Context,
    private var list: ArrayList<User>
//    private var type: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // A global variable for OnClickListener interface.
    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.shop_list, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder) {
            if(model.image != "") {
                GlideLoader(context).loadShopPicture(
                    model.image,
                    holder.itemView.findViewById(R.id.sl_image)
                )
            }
            holder.itemView.findViewById<TextView>(R.id.sl_shop_name).text = model.shop_name
            holder.itemView.findViewById<TextView>(R.id.sl_shoper_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.sl_location).text = model.city
            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

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

        fun onClick(position: Int, product: User)
    }
}