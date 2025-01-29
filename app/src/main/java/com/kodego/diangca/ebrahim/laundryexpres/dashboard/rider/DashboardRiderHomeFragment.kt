package com.kodego.diangca.ebrahim.laundryexpres.dashboard.rider

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentDashboardRiderHomeBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.CustomerRequest
import com.kodego.diangca.ebrahim.laundryexpres.model.RiderStatus
import com.kodego.diangca.ebrahim.laundryexpres.model.User
import com.kodego.diangca.ebrahim.laundryexpres.model.declinedRequest
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID


class DashboardRiderHomeFragment(var dashboardRider: DashboardRiderActivity) : Fragment() {

    private var _binding: FragmentDashboardRiderHomeBinding? = null
    private val binding get() = _binding!!

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var user: User? = null

    private var displayName: String? = null
    private var profileImageUri: Uri? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var customerRequest: CustomerRequest

    lateinit var status: String

    private var lat: Double = 0.0
    private var lng: Double = 0.0


    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDashboardRiderHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    override fun onResume() {
        super.onResume()

        user = dashboardRider.getUser()
        val bundle = this.arguments
        if (bundle != null) {
            user = bundle.getParcelable<User>("user")!!
            Log.d("ON_RESUME_FETCH_USER", user.toString())
        }
        if (user != null) {
            setUserDetails(user!!)
        }
    }

    private fun initComponent() {
//        monitorLaundryRequests()
    }

    fun checkPendingDeliveries(riderId: String, callback: (Boolean) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("orders")
        var hasPending = false // Track if there are pending requests

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (customerSnapshot in snapshot.children) {
                    for (transactionSnapshot in customerSnapshot.children) {
                        val transactionId = customerSnapshot.key

                        val assignedRiderId =
                            transactionSnapshot.child("rid").getValue(String::class.java)
                        val customerId =
                            transactionSnapshot.child("uid").getValue(String::class.java)
                        status =
                            transactionSnapshot.child("status").getValue(String::class.java) ?: ""


                        if (assignedRiderId == riderId && (status == "TO PICK-UP" || status == "TO DELIVER")) {
                            hasPending = true // A match was found
                            val customerRef = FirebaseDatabase.getInstance()
                                .getReference("users").child(customerId!!)

                            customerRef.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    var pickupLocation = "Unknown Location"
                                    if (snapshot.key == customerId) {
                                        pickupLocation =
                                            snapshot.child("address").value as? String
                                                ?: "Unknown Location"
                                        val firstName =
                                            snapshot.child("firstname").value as? String ?: ""
                                        val lastName =
                                            snapshot.child("lastname").value as? String ?: ""

                                        getLatLngFromAddress(pickupLocation) { customerLocation ->
                                            customerLocation?.let { (customerLat, customerLng) ->
                                                getRiderLocation { riderLocation ->
                                                    val (riderLat, riderLng) = riderLocation
                                                    val distance = calculateDistance(
                                                        customerLat,
                                                        customerLng,
                                                        riderLat,
                                                        riderLng
                                                    )

                                                    customerRequest = CustomerRequest(
                                                        transactionId ?: "Unknown Transaction",
                                                        customerId,
                                                        "$firstName $lastName",
                                                        pickupLocation,
                                                        distance,
                                                        customerLat,
                                                        customerLng
                                                    )

                                                    Log.d(
                                                        "Monitor Request Pending $status",
                                                        "You have a pending $status: $customerRequest"
                                                    )
                                                    showPendingRequestDialog(customerRequest)

//                                                    Toast.makeText(
//                                                        context,
//                                                        "You have a pending $status: ${transactionSnapshot.key}",
//                                                        Toast.LENGTH_LONG
//                                                    ).show()

                                                }
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e(
                                        "Firebase",
                                        "Error fetching customer data: ${error.message}"
                                    )
                                    Toast.makeText(
                                        context,
                                        "Error fetching customer data: ${error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            });

                            callback(true) // Assume no pending delivery in case of an error
                            return
                        }
                    }
                }
                // ✅ If we reached this point without finding any match, call callback(false)
                if (!hasPending) {
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error checking pending deliveries: ${error.message}")
                Toast.makeText(
                    context,
                    "Error checking pending deliveries: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                callback(true) // Assume no pending delivery in case of an error
            }
        })
    }

    // Call this before monitoring laundry requests
    fun startMonitoringRequests(riderId: String) {
        Log.d("Monitor Request", "⚠️ Monitoring Pending...")
        checkPendingDeliveries(riderId) { hasPendingDelivery ->
            if (hasPendingDelivery) {
                Log.d("Monitor Request", "⚠️ You have a pending $status!")
                Toast.makeText(context, "You have a pending $status!", Toast.LENGTH_LONG).show()
            } else {
                Log.d("Monitor Request", "Monitoring Request...")
                monitorLaundryRequests(riderId)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUserDetails(user: User) {
        dashboardRider.showLoadingDialog()
        firebaseAuth.currentUser?.let {
            for (profile in it.providerData) {
                displayName = profile.displayName
                profileImageUri = profile.photoUrl
            }

            binding.apply {

                val profileView: ImageView = binding.profilePic

                if (!displayName.isNullOrEmpty()) {
                    Log.d("displayUserName", "Hi ${displayName}, Good Day!")
                    userDisplayName.text = displayName
                }

                if (profileImageUri != null) {
                    Log.d("profilePic_profileData", "$profileImageUri")
                    Picasso.with(context).load(profileImageUri)
                        .into(profileView);
                } else {
                    if (user!!.photoUri != null) {
                        val filename = "profile_${user!!.uid}"
                        profileImageUri = Uri.parse(user!!.photoUri)
                        val firebaseStorageReference =
                            FirebaseStorage.getInstance().reference.child("profile/$filename")
                        Log.d("PROFILE_FILENAME", filename)
                        Log.d("PROFILE_URI", profileImageUri!!.toString())
                        val localFile = File.createTempFile("temp_profile", ".jpg")
                        firebaseStorageReference.getFile(localFile)
                            .addOnSuccessListener {
                                profileView.setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
                                Log.d(
                                    "USER_PROFILE_PIC",
                                    "User Profile has been successfully load!"
                                )
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Please Update your Profile Picture",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("USER_PROFILE_PIC", "User Profile failed to load!")
                            }
//                        Log.d("profilePic_user", "$profileImageUri")
//                        Picasso.with(context).load(profileImageUri)
//                            .into(profileView);
                    }
                }

                Log.d("displayUserName", "Hi ${user.firstname} ${user.lastname}, Good Day!")
                userDisplayName.text = "Hi ${user.firstname} ${user.lastname}, Good Day!"

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d("FCM Token", "Token retrieved: $token")

                        // Save the token to the database
                        saveMessagingTokenToDatabase(token)
                    } else {
                        Log.e("FCM Token", "Failed to retrieve token", task.exception)
                    }
                }
            }
            dashboardRider.dismissLoadingDialog()

            startMonitoringRequests(user.uid!!)
        }

    }

    private fun saveMessagingTokenToDatabase(token: String) {
        val riderId = FirebaseAuth.getInstance().currentUser?.uid
        if (riderId != null) {
            val database = FirebaseDatabase.getInstance().getReference("riders")
            val tokenMap = mapOf("messagingToken" to token)

            database.child(riderId).updateChildren(tokenMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Save Token", "Messaging token saved successfully.")
                } else {
                    Log.e("Save Token", "Failed to save messaging token.", task.exception)
                }
            }
        } else {
            Log.e("Save Token", "Rider ID is null. Unable to save messaging token.")
        }
    }

    /**
    private fun monitorLaundryRequests() {
    // Reference to the "orders" node
    val firebaseDatabaseReference = FirebaseDatabase.getInstance()
    .getReferenceFromUrl("https://laundry-express-382503-default-rtdb.firebaseio.com/orders")


    Log.d("Monitor Laundry Request", "Monitoring...")

    // Add a listener for changes in the "orders"
    firebaseDatabaseReference.addValueEventListener(object : ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {
    val customerRequests = mutableListOf<CustomerRequest>()
    var processedRequestsCount = 0
    var totalRequests =
    0  // Total requests matching the condition (status == "FOR PICK-UP" && customerId != "Unknown Customer")

    if (snapshot.exists()) {
    // Count total valid customer requests that meet the condition
    for (userSnapshot in snapshot.children) {
    for (transactionSnapshot in userSnapshot.children) {
    val status = transactionSnapshot.child("status").value as? String
    val customerId = transactionSnapshot.child("uid").value as? String
    ?: "Unknown Customer"

    // Only count requests that match the given condition
    if (status == "FOR PICK-UP" && customerId != "Unknown Customer") {
    totalRequests++  // Increment the total count for valid requests
    }
    }
    }

    // Process the requests now that we know the total count
    for (userSnapshot in snapshot.children) {
    for (transactionSnapshot in userSnapshot.children) {
    val transactionId = transactionSnapshot.key
    val status = transactionSnapshot.child("status").value as? String
    val customerId = transactionSnapshot.child("uid").value as? String
    ?: "Unknown Customer"

    if (status == "FOR PICK-UP" && customerId != "Unknown Customer") {
    val customerRef = FirebaseDatabase.getInstance()
    .getReference("users").child(customerId)

    customerRef.addListenerForSingleValueEvent(object :
    ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {
    var pickupLocation = "Unknown Location"
    if (snapshot.key == customerId) {
    pickupLocation =
    snapshot.child("address").value as? String
    ?: "Unknown Location"
    val firstName =
    snapshot.child("firstname").value as? String ?: ""
    val lastName =
    snapshot.child("lastname").value as? String ?: ""

    getLatLngFromAddress(pickupLocation) { customerLocation ->
    customerLocation?.let { (customerLat, customerLng) ->
    getRiderLocation { riderLocation ->
    val (riderLat, riderLng) = riderLocation
    val distance = calculateDistance(
    customerLat,
    customerLng,
    riderLat,
    riderLng
    )

    val request = CustomerRequest(
    transactionId ?: "Unknown Transaction",
    customerId,
    "$firstName $lastName",
    pickupLocation,
    distance,
    customerLat,
    customerLng
    )

    // Add the request to the list
    customerRequests.add(request)

    Log.d(
    "Monitor ListRequestedData",
    "Transaction ID: $transactionId\n" +
    "Customer ID: $customerId\n" +
    "Customer name: $firstName $lastName\n" +
    "Pickup Location: $pickupLocation\n" +
    "Monitor Distance from customer to rider: $distance km \n" +
    "Do you want to accept this request?"
    )
    // Increment the processed count
    processedRequestsCount++

    // Check if all requests are processed
    if (processedRequestsCount == totalRequests) {
    Log.d(
    "Monitor # of Request",
    "Done loading all requests: ${customerRequests.size}"
    )

    //                                                             Trigger post-data loading actions here
    //                                                            fetchOnlineRiders { onlineRiders ->
    //                                                                val assignments =
    //                                                                    distributeRequestAmongRiders(
    //                                                                        onlineRiders,
    //                                                                        customerRequests
    //                                                                    )
    //                                                                Log.d(
    //                                                                    "Monitor Assignments",
    //                                                                    "Assignments: ${assignments.size}"
    //                                                                )
    //                                                                notifyRider(assignments)
    //                                                            }

    //Working
    val nearestRequest =
    customerRequests.minByOrNull { it.distance }
    val chosenRequest =
    chooseOneRequest(customerRequests)
    Log.d(
    "Monitor Chosen Request",
    "$chosenRequest"
    )


    notifyRider(chosenRequest)

    }
    }
    }
    }
    }
    }

    override fun onCancelled(error: DatabaseError) {
    Log.e(
    "Firebase",
    "Error fetching customer data: ${error.message}"
    )
    }
    })
    }
    }
    }
    }
    }

    override fun onCancelled(error: DatabaseError) {
    Log.e("Firebase", "Error: ${error.message}")
    }
    })
    }
     */

    private fun monitorLaundryRequests(riderId: String) {
        val firebaseDatabaseReference = FirebaseDatabase.getInstance()
            .getReference("orders")

        firebaseDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val customerRequests = mutableListOf<CustomerRequest>()
                var processedRequestsCount = 0
                var totalRequests = 0

                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        for (transactionSnapshot in userSnapshot.children) {
                            val transactionId = transactionSnapshot.key
                            val status = transactionSnapshot.child("status").value as? String
                            val customerId = transactionSnapshot.child("uid").value as? String
                                ?: "Unknown Customer"
                            val assignedRiderId =
                                transactionSnapshot.child("rid").getValue(String::class.java)

                            // ✅ Filter transactions where:
                            // - Status is "FOR PICK-UP"
                            // - Customer ID is not "Unknown Customer"
                            // - No rider is assigned yet (rid is null or empty)
                            if (status == "FOR PICK-UP" && customerId != "Unknown Customer" && assignedRiderId.isNullOrEmpty()) {
                                totalRequests++
                            }
                        }
                    }

                    for (userSnapshot in snapshot.children) {
                        for (transactionSnapshot in userSnapshot.children) {
                            val transactionId = transactionSnapshot.key
                            val status = transactionSnapshot.child("status").value as? String
                            val customerId = transactionSnapshot.child("uid").value as? String
                                ?: "Unknown Customer"
                            val assignedRiderId =
                                transactionSnapshot.child("rid").getValue(String::class.java)

                            // ✅ Only process orders without an assigned rider
                            if (status == "FOR PICK-UP" && customerId != "Unknown Customer" && assignedRiderId.isNullOrEmpty()) {
                                val customerRef = FirebaseDatabase.getInstance()
                                    .getReference("users").child(customerId)

                                customerRef.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        var pickupLocation = "Unknown Location"
                                        if (snapshot.key == customerId) {
                                            pickupLocation =
                                                snapshot.child("address").value as? String
                                                    ?: "Unknown Location"
                                            val firstName =
                                                snapshot.child("firstname").value as? String ?: ""
                                            val lastName =
                                                snapshot.child("lastname").value as? String ?: ""

                                            getLatLngFromAddress(pickupLocation) { customerLocation ->
                                                customerLocation?.let { (customerLat, customerLng) ->
                                                    getRiderLocation { riderLocation ->
                                                        val (riderLat, riderLng) = riderLocation
                                                        val distance = calculateDistance(
                                                            customerLat,
                                                            customerLng,
                                                            riderLat,
                                                            riderLng
                                                        )

                                                        val request = CustomerRequest(
                                                            transactionId ?: "Unknown Transaction",
                                                            customerId,
                                                            "$firstName $lastName",
                                                            pickupLocation,
                                                            distance,
                                                            customerLat,
                                                            customerLng
                                                        )

                                                        customerRequests.add(request)

                                                        Log.d(
                                                            "Monitor Request Data",
                                                            "Transaction ID: $transactionId\n" +
                                                                    "Customer ID: $customerId\n" +
                                                                    "Customer name: $firstName $lastName\n" +
                                                                    "Pickup Location: $pickupLocation\n" +
                                                                    "Distance: $distance km \n"
                                                        )

                                                        processedRequestsCount++
                                                        if (processedRequestsCount == totalRequests) {
                                                            Log.d(
                                                                "Monitor Request Count",
                                                                "Done loading requests: ${customerRequests.size}"
                                                            )

                                                            val chosenRequest =
                                                                chooseOneRequest(customerRequests)
                                                            Log.d(
                                                                "Monitor Chosen Request",
                                                                "$chosenRequest"
                                                            )
                                                            notifyRider(chosenRequest)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e(
                                            "Firebase",
                                            "Error fetching customer data: ${error.message}"
                                        )
                                    }
                                })
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }


    private fun fetchOnlineRiders(onRidersFetched: (List<RiderStatus>) -> Unit) {
        val ridersRef = FirebaseDatabase.getInstance().getReference("riders")

        ridersRef.orderByChild("status").equalTo("online").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val onlineRiders = mutableListOf<RiderStatus>()
                Log.d("Monitor Online Riders", "Snapshot Data: ${snapshot.value}")
                for (riderSnapshot in snapshot.children) {
                    val riderId = riderSnapshot.key ?: continue
                    val lat = riderSnapshot.child("lat").getValue(Double::class.java) ?: continue
                    val lng = riderSnapshot.child("lng").getValue(Double::class.java) ?: continue
                    val activeRequests =
                        riderSnapshot.child("activeRequest").getValue(Int::class.java) ?: 0
                    val messagingToken =
                        riderSnapshot.child("messagingToken").getValue(String::class.java)
                            ?: continue

                    onlineRiders.add(
                        RiderStatus(
                            id = riderId,
                            lat = lat,
                            lng = lng,
                            activeRequests = activeRequests,
                            messagingToken = messagingToken
                        )
                    )
                }

                Log.d("Monitor Online Riders", "Online Riders Count: ${onlineRiders.size}")
                onRidersFetched(onlineRiders)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to fetch online riders: ${error.message}")
            }
        })
    }


    private fun chooseOneRequest(requests: List<CustomerRequest>): CustomerRequest {
        val shortestDistance = requests.minByOrNull { it.distance }?.distance ?: Double.MAX_VALUE
        val nearestRequests = requests.filter { it.distance == shortestDistance }

        return when {
            nearestRequests.size == 1 -> nearestRequests.first()
            else -> {
                // Implement tie-breaking logic
                nearestRequests.minByOrNull { request ->
                    // Example: Prefer earliest transaction ID (assuming it's alphanumeric order)
                    request.transactionId
                } ?: nearestRequests.random() // Fall back to random selection
            }
        }
    }

    private fun notifyRider(request: CustomerRequest) {
        Log.d(
            "Monitor Notify Driver",
            "Transaction ID: ${request.transactionId}, Customer: ${request.customerName}, Distance: ${request.distance} km"
        )
        // Add additional logic to notify the driver about the chosen request

        showRideRequestDialog(request)
    }

    private fun showRideRequestDialog(
        customerRequest: CustomerRequest
    ) {


        val builder = AlertDialog.Builder(dashboardRider)
        builder.setCancelable(false)
        builder.setTitle("New Laundry Pickup Request")
        builder.setMessage(
            "Transaction ID: ${customerRequest.transactionId}\nCustomer ID: ${customerRequest.customerId}\n" +
                    "Customer name: ${customerRequest.customerName}\n" +
                    "Pickup Location: ${customerRequest.pickupLocation}\n" +
                    "Distance: ${customerRequest.distance} km\n\n" +
                    "Do you want to accept this request?"
        )

        builder.setPositiveButton("Accept") { _, _ ->
            openGoogleMaps(customerRequest.pickupLocation)
            // Update the status of the transaction to "accepted" in Firebase
            Log.d("Monitor Request Status", "${customerRequest.transactionId} accepted")
            updateRequestStatus(
                user!!.uid!!,
                customerRequest.customerId,
                customerRequest.transactionId,
                "TO PICK-UP"
            )

        }

        builder.setNegativeButton("Decline") { dialog, _ ->
            // Array of predefined reasons
            val reasons = arrayOf("Customer is too far", "Waiting for pending to deliver laundry", "Other (Specify)")
            var selectedReason = reasons[0] // Default selection
            var customReason: String? = null

            val reasonDialog = AlertDialog.Builder(context)
                .setTitle("Reason for Declining")
                .setSingleChoiceItems(reasons, 0) { _, which ->
                    selectedReason = reasons[which]
                }
                .setPositiveButton("Confirm") { reasonDialog, _ ->
                    if (selectedReason == "Other (Specify)") {
                        // Show an input dialog for custom reason
                        val inputReason = EditText(context)
                        inputReason.hint = "Enter your reason"

                        AlertDialog.Builder(context)
                            .setTitle("Specify Your Reason")
                            .setView(inputReason)
                            .setPositiveButton("Submit") { _, _ ->
                                customReason = inputReason.text.toString()
                                if (customReason.isNullOrBlank()) {
                                    customReason = "Not specified"
                                }
                                saveDeclinedRequest(user!!.uid!!, customerRequest, customReason!!)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    } else {
                        saveDeclinedRequest(user!!.uid!!, customerRequest, selectedReason)
                    }
                    reasonDialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }



        dialog = builder.create()
        dialog.show()
    }

    private fun saveDeclinedRequest(riderId: String, customerRequest: CustomerRequest, reason: String) {

        val declineRef = FirebaseDatabase.getInstance().getReference("declined_request/$riderId")
            .child(customerRequest.transactionId)

//        val riderStatusRef = firebaseDatabase.reference("declined_request/$riderId")
//            .child(customerRequest.transactionId)
//            .push() // Creates a unique entry for each decline event

        getRiderLocation { riderLocation ->
            val (riderLat, riderLng) = riderLocation

            val declinedRequest = declinedRequest(riderId,
                customerRequest.transactionId,
                customerRequest.customerId,
                customerRequest.customerName,
                customerRequest.pickupLocation,
                customerRequest.distance,
                customerRequest.customerLat,
                customerRequest.customerLng,
                riderLat,
                riderLng,
                reason,  // 🟢 Include the decline reason
                System.currentTimeMillis()
            )
            // Data to store
//            val declinedRequest = mapOf(
//                "riderId" to riderId,
//                "transactionId" to customerRequest.transactionId,
//                "customerId" to customerRequest.customerId,
//                "customerName" to customerRequest.customerName,
//                "pickupLocation" to customerRequest.pickupLocation,
//                "distance" to customerRequest.distance,
//                "customerLat" to customerRequest.customerLat,
//                "customerLng" to customerRequest.customerLng,
//                "riderLat" to riderLat,
//                "riderLng" to riderLng,
//                "declineReason" to reason,  // 🟢 Include the decline reason
//                "timestamp" to System.currentTimeMillis()
//            )

            declineRef.setValue(declinedRequest)
                .addOnSuccessListener {
                    Log.d("Declined Request", "Successfully stored declined request with reason: $reason")
                    Toast.makeText(context, "Request declined successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase Error", "Failed to store declined request: ${e.message}")
                }
        }
    }



    private fun showPendingRequestDialog(
        customerRequest: CustomerRequest
    ) {


        val builder = AlertDialog.Builder(dashboardRider)
        builder.setCancelable(false)
        builder.setTitle("Pending Laundry Pickup Request")
        builder.setMessage(
            "Transaction ID: ${customerRequest.transactionId}\nCustomer ID: ${customerRequest.customerId}\n" +
                    "Customer name: ${customerRequest.customerName}\n" +
                    "Pickup Location: ${customerRequest.pickupLocation}\n" +
                    "Distance: ${customerRequest.distance} km\n\n"
        )

        builder.setPositiveButton("Open Map") { _, _ ->
            openGoogleMaps(customerRequest.pickupLocation)
        }

        builder.setNegativeButton("Close") { dialog, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }

    private fun notifyRider(assignments: Map<RiderStatus, CustomerRequest?>) {
        assignments.forEach { (rider, request) ->
            if (request != null) {
                Log.d(
                    "Monitor Request Notify Rider",
                    "Rider ID: ${rider.id}, Transaction ID: ${request.transactionId}, " +
                            "Customer: ${request.customerName}, Distance: ${request.distance} km"
                )
                // Add logic to notify the rider, such as sending a push notification
//                sendNotificationToRider(rider, request)

                // Optionally, display a dialog to inform the admin or rider
                showRideRequestDialog(rider, request)
            } else {
                Log.d("Monitor Notify Rider", "No request assigned to Rider ID: ${rider.id}")
            }
        }
    }

    private fun sendNotificationToRider(rider: RiderStatus, request: CustomerRequest) {
        // Logic for sending a push notification or message to the rider
        val message = "New request assigned! Customer: ${request.customerName}, " +
                "Location: ${request.pickupLocation}, Distance: ${request.distance} km"
        // Example: Send via Firebase Cloud Messaging (FCM)
        FirebaseMessaging.getInstance().send(
            RemoteMessage.Builder("${rider.messagingToken}@fcm.googleapis.com")
                .setMessageId(UUID.randomUUID().toString())
                .addData("title", "New Ride Request")
                .addData("body", message)
                .build()
        )
        Log.d("NotifyRider", "Notification sent to Rider ID: ${rider.id}")
    }


    private fun showRideRequestDialog(
        rider: RiderStatus, customerRequest: CustomerRequest
    ) {


        val builder = AlertDialog.Builder(dashboardRider)
        builder.setCancelable(false)
        builder.setTitle("New Laundry Pickup Request")
        builder.setMessage(
            "Transaction ID: ${customerRequest.transactionId}\nCustomer ID: ${customerRequest.customerId}\n" +
                    "Customer name: ${customerRequest.customerName}\n" +
                    "Pickup Location: ${customerRequest.pickupLocation}\n" +
                    "Distance: ${customerRequest.distance} km\n\n" +
                    "Do you want to accept this request?"
        )

        builder.setPositiveButton("Accept") { _, _ ->
            openGoogleMaps(customerRequest.pickupLocation)
            // Update the status of the transaction to "accepted" in Firebase
            Log.d("Monitor Request Status", "${customerRequest.transactionId} accepted")
            updateRequestStatus(
                rider.id,
                customerRequest.customerId,
                customerRequest.transactionId,
                "TO PICK-UP"
            )

        }

        builder.setNegativeButton("Decline") { dialog, _ ->
            dialog.dismiss()
        }

        dialog = builder.create()
        dialog.show()
    }

    fun updateRequestStatus(
        riderId: String,
        customerId: String,
        transactionId: String,
        newStatus: String
    ) {
        // Get a reference to the Firebase Database
        val databaseReference = FirebaseDatabase.getInstance().getReference("orders/$customerId")

        // Update the status and add rider on Transaction of the transaction
        databaseReference.child(transactionId).child("status").setValue(newStatus)
        databaseReference.child(transactionId).child("rid").setValue(riderId)

//            .addOnSuccessListener {
//                val riderStatusRef = firebaseDatabase.getReference("riders").child(riderId)
//
//
//                riderStatusRef.child("transactionId").setValue(transactionId)
//
//                riderStatusRef.child("activeRequest").get()
//                    .addOnSuccessListener { activeRequestsSnapshot ->
//
//                        var activeRequest = activeRequestsSnapshot.getValue(Int::class.java) ?: 0
//
//                        if (!activeRequestsSnapshot.exists()) {
//                            activeRequest = 1
//                        } else {
//                            activeRequest++
//                        }
//
//                        riderStatusRef.child("activeRequest").setValue(activeRequest)
//                        Log.d(
//                            "Monitor Request Status",
//                            "$transactionId Status updated to: $newStatus"
//                        )
//                    }
//                Log.d("Monitor Request Status", "$transactionId Status updated to: $newStatus")
//            }
//            .addOnFailureListener { exception ->
//                Log.e(
//                    "Monitor Request Status",
//                    "$transactionId Error updating status: ${exception.message}"
//                )
//            }
    }


    fun openGoogleMaps(pickupLocation: String) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$pickupLocation")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    /** GOOGLE MAP
    fun getLatLngFromAddress(address: String, callback: (Pair<Double, Double>?) -> Unit) {
    val url = "https://maps.googleapis.com/maps/api/geocode/json?address=$address&key=AIzaSyAzofW3ZdxI6l6Jz7C16Z_iHHLH_KMmIWY"

    // Using an HTTP library like OkHttp or Retrofit to call the API
    val request = OkHttpClient().newCall(
    Request.Builder().url(url).build()
    )
    request.enqueue(object : Callback {
    override fun onResponse(call: Call, response: Response) {
    try {
    response.body?.string()?.let { responseBody ->
    val jsonObject = JSONObject(responseBody)
    val resultsArray = jsonObject.getJSONArray("results")

    // Check if the "results" array is not empty
    if (resultsArray.length() > 0) {
    val location = resultsArray
    .getJSONObject(0)
    .getJSONObject("geometry")
    .getJSONObject("location")
    val lat = location.getDouble("lat")
    val lng = location.getDouble("lng")
    callback(Pair(lat, lng))
    } else {
    // No results found
    callback(null)
    }
    } ?: callback(null)
    } catch (e: JSONException) {
    // Handle JSON parsing error
    e.printStackTrace()
    callback(null)
    }
    }

    override fun onFailure(call: Call, e: IOException) {
    // Handle network failure
    e.printStackTrace()
    callback(null)
    }
    })
    }
     */

    /** OPENCAGEDATA  */

    fun getLatLngFromAddress(address: String, callback: (Pair<Double, Double>?) -> Unit) {
        val apiKey = "79d1f473d05142f8a1add65e93dcf25d" // Replace with your OpenCage API key
        val url = "https://api.opencagedata.com/geocode/v1/json?q=$address&key=$apiKey"

        // Using OkHttp to make the network call
        val request = OkHttpClient().newCall(
            Request.Builder().url(url).build()
        )
        request.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    // Get the response body
                    response.body?.string()?.let { responseBody ->
                        val jsonObject = JSONObject(responseBody)
                        val resultsArray = jsonObject.getJSONArray("results")

                        // Check if results array is not empty
                        if (resultsArray.length() > 0) {
                            val geometry = resultsArray.getJSONObject(0).getJSONObject("geometry")
                            val lat = geometry.getDouble("lat")
                            val lng = geometry.getDouble("lng")
                            callback(Pair(lat, lng))  // Return the latitude and longitude
                        } else {
                            // No results found
                            callback(null)
                        }
                    } ?: callback(null)
                } catch (e: JSONException) {
                    // Handle JSON parsing error
                    e.printStackTrace()
                    callback(null)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle network failure
                e.printStackTrace()
                callback(null)
            }
        })
    }


    fun getRiderLocation(callback: (Pair<Double, Double>) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(dashboardRider)
        if (ActivityCompat.checkSelfPermission(
                dashboardRider,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                dashboardRider,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Request the missing permissions
            ActivityCompat.requestPermissions(
                dashboardRider,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lat = location.latitude
                lng = location.longitude
                callback(Pair(lat, lng))
            } else {
                Log.e("Monitor Rider Location", "Rider location not available")
            }
        }.addOnFailureListener {
            Log.e("Error Monitor Rider Location", "Failed to get location")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with location fetching
                    Log.d("Permissions", "Location permission granted!")
                    getRiderLocation { riderLocation ->
                        val (riderLat, riderLng) = riderLocation
                        Log.d("Rider", "Lat: $riderLat, Lng: $riderLng")
                    }
                } else {
                    // Permission denied, handle gracefully
                    Log.e("Permissions", "Location permission denied!")
                    Toast.makeText(
                        dashboardRider,
                        "Location permission is required to fetch rider location.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c // Distance in kilometers
    }

    private fun distributeRequestAmongRiders(
        riders: List<RiderStatus>,
        requests: List<CustomerRequest>
    ): Map<RiderStatus, CustomerRequest?> {
        // Maintain a map of rider to assigned request
        val riderAssignments = mutableMapOf<RiderStatus, CustomerRequest?>()
        val unassignedRequests = requests.toMutableList() // Keep track of unassigned requests

        for (request in requests) {
            // Get all riders with the same distance to this request
            val availableRiders = riders.filter { rider ->
                calculateDistance(
                    rider.lat,
                    rider.lng,
                    request.customerLat,
                    request.customerLng
                ) == request.distance
            }

            // Find the rider with the least workload
            val chosenRider = availableRiders.minByOrNull { it.activeRequests } ?: continue

            // Assign the request to the chosen rider
            riderAssignments[chosenRider] = request

            // Update rider's workload (simulate database update)
            chosenRider.activeRequests += 1

            unassignedRequests.remove(request)
        }

        return riderAssignments
    }


}
