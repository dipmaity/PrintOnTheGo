package com.example.plk.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.plk.AddEditAddressActivity
import com.example.plk.AddressListActivity
import com.example.plk.BasekFragment
import com.example.plk.ChooserActivity
import com.example.plk.MainActivity
import com.example.plk.OrderDetailsActivity
import com.example.plk.models.Orderitemslist
import com.example.plk.models.Thikana
import com.example.plk.models.User
import com.example.plk.ui.LoginActivity
import com.example.plk.ui.RegisterActivity
import com.example.plk.ui.UserProfileActivity
import com.example.plk.ui.dashboard.OrderFragment
import com.example.plk.ui.home.ShopFragment
import com.example.plk.ui.profile.ProfileFragment
import com.example.plk.uploadActivity
import com.example.plk.utils.Constants
import com.facebook.login.Login
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {
    // Access a Cloud Firestore instance.
    private val mFireStore = FirebaseFirestore.getInstance()

    fun getUserDetails(activity: Fragment) {

        // Here we pass the collection name from which we wants the data.

        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                // Here we have received the document snapshot which is converted into the User Data model object.
                Log.e("print", "first")
                val user = document.toObject(User::class.java)!!
                Log.e("print", "second")
//                val sharedPreferences =
//                    activity.activity?.getSharedPreferences(
//                        Constants.MYAPP_PREFERENCES,
//                        Context.MODE_PRIVATE
//                    )
//
//                // Create an instance of the editor which is help us to edit the SharedPreference.
//                val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
//                editor?.putString(
//                    Constants.LOGGED_IN_USERNAME,
//                    user.name
//                )
//                editor?.apply()

                when (activity) {
                    is ProfileFragment -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userDetailsSuccess(user)
                    }
                    is OrderFragment -> {
                        activity.userDetailsSuccess(user)
                    }
//                    is BasekFragment -> {
//                        activity.userDetails(user)
//                    }

                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is ProfileFragment -> {
                        activity.hideProgressDialog()
                    }
                    is OrderFragment -> {
                        activity.hideProgressDialog()
                    }
//                    is BasekFragment -> {
//                        activity.hideProgressDialog()
//                    }
                }
            }
    }

    /**
     * A function to get the logged user details from from FireStore Database.
     */
    fun getUserDetails(activity: Activity) {

        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                // Here we have received the document snapshot which is converted into the User Data model object.
                val user = document.toObject(User::class.java)!!

                val sharedPreferences =
                    activity.getSharedPreferences(
                        Constants.MYAPP_PREFERENCES,
                        Context.MODE_PRIVATE
                    )

                // Create an instance of the editor which is help us to edit the SharedPreference.
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString(
                    Constants.LOGGED_IN_USERNAME,
                    user.name
                )
                editor.apply()

                when (activity) {
                    is LoginActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        Log.e("forth", "4")
                        activity.userLoggedInSuccess(user)
                    }

                    is uploadActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userLoggedInSuccess(user)
                    }

                    is MainActivity -> {
//                        activity.userLoggedInSuccess(user)
                    }

                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }
                    is uploadActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
//                        activity.hideProgressDialog()
                    }

                }
            }
    }



    /**
     * A function to get the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        Log.e("second", "2")
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        Log.e("ID", "${currentUserID}")
        return currentUserID
    }

    /**
     * A function to make an entry of the registered user in the FireStore database.
     */
    fun registerUser(activity: Activity, userInfo: User) {
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(userInfo.id)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                when(activity) {
                    is RegisterActivity -> {
                        activity.userRegistrationSuccess()
                    }

                    is ChooserActivity -> {
                        activity.userRegistrationSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when(activity) {
                    is RegisterActivity -> {
                        activity.hideProgressDialog()
                        Toast.makeText(
                            activity,
                            "Registration Failed! Due to technical fault",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }


    // A function to upload the image to the cloud storage.
    fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?, imageType: String) {

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() + "."
                    + Constants.getFileExtension(
                activity,
                imageFileURI
            )
        )

        //adding the file to reference
        sRef.putFile(imageFileURI!!)
            .addOnSuccessListener { taskSnapshot ->

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->

                        // Here call a function of base activity for transferring the result to it.
                        when (activity) {
                            is UserProfileActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }

                            is AddressListActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }

//                            is AddProductActivity -> {
//                                activity.imageUploadSuccess(uri.toString())
//                            }
                        }
                    }
            }
            .addOnFailureListener { exception ->

                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }

                    is AddressListActivity -> {
                        activity.hideProgressDialog()
                    }

//                    is AddProductActivity -> {
//                        activity.hideProgressDialog()
//                    }
                }
            }
    }

    /**
     * A function to update the user profile data into the database.
     *
     * @param activity The activity is used for identifying the Base activity to which the result is passed.
     * @param userHashMap HashMap of fields which are to be updated.
     */
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        // Collection Name
        mFireStore.collection(Constants.USERS)
            // Document ID against which the data to be updated. Here the document id is the current logged in user id.
            .document(getCurrentUserID())
            // A HashMap of fields which are to be updated.
            .update(userHashMap)
            .addOnSuccessListener {

                // Notify the success result.
                when (activity) {
                    is UserProfileActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userProfileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->

                when (activity) {
                    is UserProfileActivity -> {
                        // Hide the progress dialog if there is any error. And print the error in log.
                        activity.hideProgressDialog()
                    }
                }

            }
    }

    /**
     * A function to get the dashboard items list. The list will be an overall items list, not based on the user's id.
     */
    fun getDashboardItemsList(fragment: ShopFragment) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.USERS)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<User> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    user.id = i.id
                    if(user.type == Constants.SHOPER)
                        productsList.add(user)
                }

                // Pass the success result to the base fragment.
                fragment.successDashboardItemsList(productsList)
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error which getting the dashboard items list.
                fragment.hideProgressDialog()
            }
    }

    /**
     * A function to get the dashboard items list. The list will be an overall items list, not based on the user's id.
     */
    fun getOrderItemsList(fragment: OrderFragment, user:User, check:String) {
        mFireStore.collection(Constants.ORDERS)
            .whereEqualTo(check, user)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                val list: ArrayList<Orderitemslist> = ArrayList()

                for(i in document.documents) {

                    val oderItem = Orderitemslist(
                        i.getField<User>("customer"),
                        i.getField<User>("shoper"),
                         null,
                        i.getField<String>("path"),
                        i.getField<Thikana>("address"),
                        i.getField<String>("fileURL"),
                        i.id,
                        i.getField<Boolean>("download")

                    )
                    updateId(oderItem)
                    list.add(oderItem)
                }


                // TODO Step 7: Notify the success result to base class.
                // START
                fragment.populateOrdersListInUI(list)
                // END
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.

                fragment.hideProgressDialog()

                Log.e(fragment.javaClass.simpleName, "Error while getting the orders list.", e)
            }
    }

    private fun updateId(oderItem: Orderitemslist) {
        mFireStore.collection(Constants.ORDERS)
            .document(oderItem.id)
            .set(oderItem, SetOptions.merge())
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }

    }


    /**
     * A function to get the list of address from the cloud firestore.
     *
     * @param activity
     */
    fun getAddressesList(activity: AddressListActivity) {
        mFireStore.collection(Constants.ADDRESSES)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
//                // Here we get the list of boards in the form of documents.
//               // Here we have created a new instance for address ArrayList.
                val addressList: ArrayList<Thikana> = ArrayList()
//                // A for loop as per the list of documents to convert them into Boards ArrayList.
                for(j in document.documents) {
                    val address = j.toObject(Thikana::class.java)!!
//
                        address.id = j.id
//
                        addressList.add(address)
                    Log.e("print", "infifth")
                }

                activity.successAddressListFromFirestore(addressList)
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.

                activity.hideProgressDialog()
                Toast.makeText(activity,"Technical Fault", Toast.LENGTH_SHORT ).show()
            }
    }

    /**
     * A function to delete the existing address from the cloud firestore.
     *
     * @param activity Base class
     * @param addressId existing address id
     */
    fun deleteAddress(activity: AddressListActivity, addressId: String) {

        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .delete()
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.deleteAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()

            }
    }

    fun deleteOrder(fragment: OrderDetailsActivity, orderId:String) {
        mFireStore.collection(Constants.ORDERS)
            .document(orderId)
            .delete()
            .addOnSuccessListener {
                fragment.orderDeleteSuccess()
            }
            .addOnFailureListener { e ->
                fragment.hideProgressDialog()

            }
    }

    /**
     * A function to get the product details based on the product id.
     */
//    fun getProductDetails(activity: uploadActivity, productId: String) {
//
//        // The collection name for PRODUCTS
//        mFireStore.collection(Constants.USERS)
//            .document(productId)
//            .get() // Will get the document snapshots.
//            .addOnSuccessListener { document ->
//
//                // Convert the snapshot to the object of Product data model class.
//                val product = document.toObject(User::class.java)!!
//
//                activity.productDetailsSuccess(product)
//            }
//            .addOnFailureListener { e ->
//
//                // Hide the progress dialog if there is an error.
//                activity.hideProgressDialog()
//             }
//    }

    fun placeOrder(activity: AddressListActivity, order: Orderitemslist) {

        mFireStore.collection(Constants.ORDERS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(order, SetOptions.merge())
            .addOnSuccessListener {



                // Here call a function of base activity for transferring the result to it.
                activity.orderPlacedSuccess()
            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is any error.
                activity.hideProgressDialog()

            }
    }

    fun updateOrder(activity:Activity, order:Orderitemslist) {
        mFireStore.collection(Constants.ORDERS)
            .document(order.id)
            .set(order, SetOptions.merge())
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
    }

    fun updateAddress(activity: AddEditAddressActivity, addressInfo: Thikana, addressId: String) {

        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating the Address.",
                    e
                )
            }
    }


    fun addAddress(activity: AddEditAddressActivity, addressInfo: Thikana) {

        // Collection name address.
        mFireStore.collection(Constants.ADDRESSES)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
//            .set(addressInfo, SetOptions.merge())
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {

                // TODO Step 5: Notify the success result to the base class.
                // START
                // Here call a function of base activity for transferring the result to it.
                activity.addUpdateAddressSuccess()
                // END
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while adding the address.",
                    e
                )
            }
    }

    fun getUserDetailsWithId(activity: Activity, uid: String) {
        mFireStore.collection(Constants.USERS)
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                // Here we have received the document snapshot which is converted into the User Data model object.
                Log.e("third", "3")
                val user = document.toObject(User::class.java)!!

                val sharedPreferences =
                    activity.getSharedPreferences(
                        Constants.MYAPP_PREFERENCES,
                        Context.MODE_PRIVATE
                    )

                // Create an instance of the editor which is help us to edit the SharedPreference.
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString(
                    Constants.LOGGED_IN_USERNAME,
                    user.name
                )
                editor.apply()

                when(activity) {
                    is LoginActivity -> {
                        activity.userLoggedInSuccess(user)
                    }
                    is RegisterActivity -> {
                        activity.userLoggedInSuccess(user)
                    }
                }



            }
            .addOnFailureListener {e ->
                when(activity) {
                    is LoginActivity -> {

                    }

                    is RegisterActivity -> {

                    }
                }

            }
    }

    fun getOrder(activity:OrderFragment,id:String) {
        mFireStore.collection(Constants.ORDERS)
            .document(id)
            .get()
            .addOnSuccessListener { document->
                val order = document.toObject(Orderitemslist::class.java)
                activity.getOrderDetailsSuccess(order!!)

            }
            .addOnFailureListener { e->

            }
    }


}