package com.example.plk.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
@Parcelize
data class Thikana(
    val user_id: String = "",
    val name: String = "",
    val mobileNumber: String = "",

    val address: String = "",
    val zipCode: String = "",
    val additionalNote: String = "",

    val type: String = "",
    val otherDetails: String = "",
    var id: String = "",
): Parcelable
