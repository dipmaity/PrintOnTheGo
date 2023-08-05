package com.example.plk.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Orderitemslist(
    val customer: User? = null,
    val shoper: User? = null,
    val file: Uri? = null,
    val path: String? = null,
    val address: Thikana? = null,
    val fileURL: String? = null,
    var id:String = "",
    var download:Boolean? = false
): Parcelable
