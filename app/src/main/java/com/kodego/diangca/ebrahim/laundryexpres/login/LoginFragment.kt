package com.kodego.diangca.ebrahim.laundryexpres.login

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kodego.diangca.ebrahim.laundryexpres.MainFragment
import com.kodego.diangca.ebrahim.laundryexpres.R
import com.kodego.diangca.ebrahim.laundryexpres.dashboard.customer.DashboardCustomerFragment
import com.kodego.diangca.ebrahim.laundryexpres.databinding.DialogUserTypeBinding
import com.kodego.diangca.ebrahim.laundryexpres.databinding.FragmentLoginBinding
import com.kodego.diangca.ebrahim.laundryexpres.model.User

class LoginFragment(var mainFragment: MainFragment) : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var customDialogBinding: DialogUserTypeBinding

    private lateinit var client: GoogleSignInClient

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var firebaseStorageReference: StorageReference
    private lateinit var firebaseDatabaseReference: DatabaseReference


    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var credential: AuthCredential

    private var userType = "UNKNOWN"

    fun setUserType(userType: String) {
        this.userType = userType
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initComponent() {
        firebaseAuth = mainFragment.indexActivity.getFirebaseAuth()

        firebaseDatabase = mainFragment.indexActivity.getFirebaseDatabase()
        firebaseDatabaseReference = mainFragment.indexActivity.getDatabaseReference()
        firebaseStorageReference = FirebaseStorage.getInstance().getReference("user_profile")

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestId()
            .requestProfile()
            .build()
        client = GoogleSignIn.getClient(mainFragment.indexActivity, options)
        binding.btnGoogle.setOnClickListener {
            val googleSignInIntent = client.signInIntent
//            startActivityForResult(intent, 10001)
            launcher.launch(googleSignInIntent)
        }

        binding.btnHome.setOnClickListener {
            btnHomeOnClickListener()
        }

        binding.btnSubmit.setOnClickListener {
            btnSubmitOnClickListener()
        }

        binding.btnRegister.setOnClickListener {
            btnRegisterOnClickListener()
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode==Activity.RESULT_OK) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                    if (task!=null) {
                        handleResults(task)
                    }
                } catch (e: Exception) {
                    Log.d("GOOGLE_SIGN_IN", "${e.message}")
                }
            }
        }


    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {

            val account: GoogleSignInAccount? = task.result
            if (account!=null) {
                updateUI(account)
            }
        } else {
            Toast.makeText(
                mainFragment.indexActivity,
                "HANDLE_RESULT ${task.exception.toString()}",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun updateUI(account: GoogleSignInAccount) {
        credential = GoogleAuthProvider.getCredential(account.idToken, null)

        if (userType=="UNKNOWN") {

            Log.d("UPDATE_UI", userType)
            userTypeDialog().show()

        } else {
            Log.d("UPDATE_UI_CHECK_USER", userType)

            checkUserAccount()

        }

    }

    private fun checkUserAccount() {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    showProgressBar(true)
                    firebaseDatabaseReference.child("users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {
                                    Toast.makeText(
                                        context,
                                        "User is already Registered!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    goToDashboard()
                                } else {
                                    val databaseRef = firebaseDatabase.reference.child("users")
                                        .child(firebaseAuth.currentUser!!.uid)

                                    var user = User()

                                    val firebaseAuthUser = firebaseAuth.currentUser
                                    firebaseAuthUser?.let {
                                        for (profile in it.providerData) {
                                            // Id of the provider (ex: google.com)
                                            val providerId = profile.providerId
                                            // UID specific to the provider
                                            val uid = profile.uid
                                            // Name, email address, and profile photo Url
                                            val firstName = profile.displayName
                                            val email = profile.email
                                            val photoUrl = profile.photoUrl
                                            val phone = profile.phoneNumber

                                            user = User(
                                                firebaseAuth.currentUser!!.uid,
                                                firstName,
                                                null,
                                                email,
                                                userType,
                                                providerId,
                                                phone,
                                                false,
                                                photoUrl.toString()
                                            )

                                           /* if (user.photoUrl!=null) {
                                                firebaseStorageReference.child("users/${user.uid}")
                                                    .putFile(photoUrl!!)
                                            }*/
                                        }
                                    }

                                    if(user != null){
                                        user.printLOG()
                                    }

                                    databaseRef.setValue(user).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "User has been successfully Registered!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            goToDashboard()
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    mainFragment.indexActivity,
                                    "${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    //
                } else {
                    Toast.makeText(
                        mainFragment.indexActivity,
                        it.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun goToDashboard() {
        when (userType) {
            "Customer" -> {
                mainFragment.indexActivity.mainFrame = mainFragment.indexActivity.supportFragmentManager.beginTransaction()
                mainFragment.indexActivity.mainFrame.replace(R.id.mainFrame,DashboardCustomerFragment(mainFragment.indexActivity))
                mainFragment.indexActivity.mainFrame.commit()
                showProgressBar(false)
            }
            "Partner" -> {
                showProgressBar(false)
                //Checking if Verified

            }
            "Rider" -> {
                showProgressBar(true)
            }
            else -> {

                showProgressBar(true)
            }
        }
    }

    private fun showProgressBar(visible: Boolean) {
        if (visible) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }


    private fun userTypeDialog(): Dialog {
        return activity?.let {

            customDialogBinding = DialogUserTypeBinding.inflate(this.layoutInflater)

            with(customDialogBinding) {
                userPartner.setOnClickListener {
                    userGroup.clearCheck()
                    userPartner.isChecked = true
                    userType = "Partner"
                }
                userRider.setOnClickListener {
                    userGroup.clearCheck()
                    userRider.isChecked = true
                    userType = "Rider"
                }
                userCustomer.setOnClickListener {
                    userGroup.clearCheck()
                    userCustomer.isChecked = true
                    userType = "Customer"
                }
            }

            val builder = AlertDialog.Builder(it)
            with(builder) {
                setView(customDialogBinding.root)
                setPositiveButton("Select",
                    DialogInterface.OnClickListener { dialog, id -> //to sign in the user
                        checkUserAccount()
                    })
                setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
//                              getDialog().Cancel()
                    })

                builder.create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 10001){
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                if(task.isSuccessful) {
                    val account = task.getResult(ApiException::class.java)
                    if(account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        firebaseAuth.signInWithCredential(credential)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {

                                    //

                                }
                            }
                    }
                }
            }catch (e: Exception){
                Log.d("GOOGLE_SIGN_IN", "${e.message}")
            }
        }
    }*/
    private fun btnRegisterOnClickListener() {

    }

    private fun btnHomeOnClickListener() {
        var home = MainFragment(mainFragment.indexActivity)
        home.setSelectedTab(2)
        mainFragment.indexActivity.mainFrame =
            mainFragment.indexActivity.supportFragmentManager.beginTransaction()
        mainFragment.indexActivity.mainFrame.replace(R.id.mainFrame, home)
        mainFragment.indexActivity.mainFrame.commit()
    }

    private fun btnSubmitOnClickListener() {

        val username = binding.username.text.toString()
        val password = binding.password.text.toString()

        binding.passwordLayout.isPasswordVisibilityToggleEnabled = true

        if (username.isEmpty() || password.isEmpty()) {
            if (username.isEmpty()) {
                binding.username.error = "Please enter your Mobile No."
            }
            if (password.isEmpty()) {
                binding.passwordLayout.isPasswordVisibilityToggleEnabled = false
                binding.password.error = "Please enter your Firstname."
            }
            if (!isValidEmail(username)) {
                binding.username.error = "Please enter an email or a valid email."
            }
            Toast.makeText(context, "Please check following error(s)!", Toast.LENGTH_SHORT).show()
            return
        } else {


            firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener {
                showProgressBar(true)
                Log.d("SIGN_IN_WITH_EMAIL_PASSWORD", it.toString())
                if (it.isSuccessful) {
                    firebaseDatabaseRecord()
                } else {
                    Toast.makeText(
                        context,
                        "Either your username or password is Invalid! Please try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressBar.visibility = View.GONE
                }
            }

        }

    }

    private fun firebaseDatabaseRecord() {
        firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {

                    Log.d("ForSingleValueEvent", firebaseAuth.currentUser!!.uid)

                    userType = snapshot.child(firebaseAuth.currentUser!!.uid).child("type")
                        .getValue(String::class.java).toString()

                    goToDashboard()

                } else {
                    Log.d(
                        "NOT FOUND",
                        "Either your username or password is Invalid! Please try again!"
                    )
                    Toast.makeText(
                        context,
                        "Either your username or password is Invalid! Please try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(context, "${error.message}", Toast.LENGTH_SHORT).show()
                showProgressBar(false)
            }

        })
    }

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser!=null) {

            firebaseDatabaseReference.child("users").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(firebaseAuth.currentUser!!.uid)) {

                        val getUserType =
                            snapshot.child(firebaseAuth.currentUser!!.uid).child("type")
                                .getValue(String::class.java).toString()

                        when (getUserType) {
                            "Partner" -> {

                            }
                            "Customer" -> {
                                Toast.makeText(
                                    context,
                                    "Hi ${
                                        snapshot.child(firebaseAuth.currentUser!!.uid)
                                            .child("firstname").getValue(String::class.java)
                                    }! Have a great day!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                mainFragment.indexActivity.mainFrame =
                                    mainFragment.indexActivity.supportFragmentManager.beginTransaction()
                                mainFragment.indexActivity.mainFrame.replace(
                                    R.id.mainFrame,
                                    DashboardCustomerFragment(mainFragment.indexActivity)
                                )
                                mainFragment.indexActivity.mainFrame.commit()
                            }
                            "Rider" -> {

                            }

                        }

                    } else {
                        Toast.makeText(
                            context,
                            "Either your username or password is Invalid! Please try again!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(context, "${error.message}", Toast.LENGTH_SHORT).show()
                }

            })

        }
    }
}