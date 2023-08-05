package com.example.plk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var id:String = "",
    var type:String = "",
    val name:String = "",
    val email: String = "",
    val shop_name:String = "",
    val city:String = "",
    val image: String = "",
    val mobile: Long = 0,
    val profileCompleted: Int = 0
) : Parcelable