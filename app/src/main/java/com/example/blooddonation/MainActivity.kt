package com.example.blooddonation

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.blooddonation.fragments.*
import com.example.blooddonation.models.Donation
import com.example.blooddonation.utils.ProgressDialog
import com.example.blooddonation.viewmodels.DonationViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var donationViewModel: DonationViewModel
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userDatabase: DatabaseReference
    private lateinit var donationsDatabase: DatabaseReference
    private lateinit var userId: String

    // logged in user data
    private var username: String? = null
    private var email: String? = null
    private var bloodGroup: String? = null
    private var phoneNumber: String? = null

    private lateinit var toggleNavigationDrawer: ActionBarDrawerToggle      // the button in the action bar
    private lateinit var navigationView: NavigationView     // the actual nav drawer that would open and close
    private lateinit var drawerLayout: DrawerLayout         // the layout the will host our activity and nav drawer

    private val findDonorFragmentTag = "FIND_DONOR"
    private val donateBloodFragmentTag = "DONATE_BLOOD"
    private val homeFragmentTag = "HOME"
    private val nearestHospitalFragmentTag = "NEAREST_HOSPITAL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        donationViewModel = ViewModelProvider(this).get(DonationViewModel::class.java)
        userDatabase = FirebaseDatabase.getInstance().getReference("users")
        donationsDatabase = FirebaseDatabase.getInstance().getReference("donations")

        drawerLayout =
            findViewById(R.id.drawer_layout)     // getting hold of container drawer layout
        toggleNavigationDrawer =
            ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.open,
                R.string.close
            )    // setting up drawerToggle in action bar by calling its constructor
        drawerLayout.addDrawerListener(toggleNavigationDrawer)      // adding listener to container drawer layout by passing in the navigation drawer button in the action bar
        toggleNavigationDrawer.syncState()      // syncing their state

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fragment = supportFragmentManager.beginTransaction()
        fragment.replace(R.id.fragment_container, HomeFragment()).commit()
        setToolBarTitle("Home")

        donationViewModel.donationModel.observe(this, Observer {
            val donation = Donation(
                it.donorUsername,
                it.donorEmail,
                it.donorPhoneNumber,
                it.donorBloodGroup,
                it.unitsDonated
            )
            writeDonationToDatabase(donation)
            MaterialAlertDialogBuilder(this).setMessage("You have successfully donated ${it.unitsDonated} Units of ${it.donorBloodGroup} blood group.")
                .setPositiveButton("Okay") { dialog, which ->
                    val fragmentManager = supportFragmentManager
                    val transaction = fragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, HomeFragment())
                    transaction.commit()
                }.show()
        })
    }

    override fun onStart() {
        super.onStart()
        Log.d("Main activity", "onStart: called")
        val user = mAuth.currentUser
        if (user == null) {
            //there is no user logged in
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        } else if (username.isNullOrEmpty() || email.isNullOrEmpty() || phoneNumber.isNullOrEmpty() || bloodGroup.isNullOrEmpty()) {
            navigationView = findViewById(R.id.navigation_view)
            navigationView.setNavigationItemSelectedListener(this)
            val headerView: View = navigationView.getHeaderView(0)
            val navDrawerHeaderUsernameTextView: TextView = headerView.findViewById(R.id.username)
            val navDrawerUserProfilePictureImageView: ShapeableImageView =
                headerView.findViewById(R.id.user_image)

            userId = mAuth.uid.toString()

            val progressDialog = ProgressDialog.progressDialog(this)
            progressDialog.show()
            userDatabase.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    username = snapshot.child("username").value.toString()
                    email = snapshot.child("email").value.toString()
                    bloodGroup = snapshot.child("bloodGroup").value.toString()
                    phoneNumber = snapshot.child("phoneNumber").value.toString()
                    if (snapshot.child("firebaseImageUrl").value.toString().isNotBlank()) {
                        Glide.with(this@MainActivity)
                            .load(snapshot.child("firebaseImageUrl").value.toString())
                            .centerCrop()
                            .into(navDrawerUserProfilePictureImageView)
                    }
                    navDrawerHeaderUsernameTextView.text = username
                    progressDialog.hide()
                    progressDialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggleNavigationDrawer.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val bundle = Bundle()
        bundle.putString("username", username)
        bundle.putString("email", email)
        bundle.putString("phoneNumber", phoneNumber)
        bundle.putString("bloodGroup", bloodGroup)

        when (item.itemId) {
            R.id.item_home -> {
                setToolBarTitle("Home")

                if (supportFragmentManager.findFragmentByTag(homeFragmentTag)?.isVisible == true) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return false
                }

                val fragmentManager = supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, HomeFragment(), homeFragmentTag)
                transaction.commit()
            }
            R.id.item_donate_blood -> {
                setToolBarTitle("Donate Blood")

                if (supportFragmentManager.findFragmentByTag(donateBloodFragmentTag)?.isVisible == true) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return false
                }

                val donateBloodFragment = DonateBloodFragment()
                donateBloodFragment.arguments = bundle
                val fragmentManager = supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(
                    R.id.fragment_container,
                    donateBloodFragment,
                    donateBloodFragmentTag
                )
                transaction.commit()
            }
            R.id.item_find_donor -> {
                setToolBarTitle("Find Donor")

                if (supportFragmentManager.findFragmentByTag(findDonorFragmentTag)?.isVisible == true) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return false
                }

                val fragmentManager = supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(
                    R.id.fragment_container,
                    FindDonorFragment(),
                    findDonorFragmentTag
                )
                transaction.commit()
            }
            R.id.item_find_hospital -> {
                setToolBarTitle("Find Hospital")
                Log.d("MainActivity", "onNavigationItemSelected: clicked")

                if (supportFragmentManager.findFragmentByTag(nearestHospitalFragmentTag)?.isVisible == true) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return false
                }
                if (!checkForLocationPermission()) {
                    requestFineLocationPermission()
                    requestCoarseLocationPermission()
                } else if (checkForLocationPermission()) {
                    val fragmentManager = supportFragmentManager
                    val transaction = fragmentManager.beginTransaction()
                    transaction.replace(
                        R.id.fragment_container,
                        FindHospitalFragment(),
                        nearestHospitalFragmentTag
                    )
                    transaction.commit()
                }
            }
            R.id.item_logout -> {
                mAuth.signOut()
                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    // change title of toolbar according to fragment
    private fun setToolBarTitle(title: String) {
        supportActionBar?.title = title
    }

    private fun writeDonationToDatabase(donation: Donation) {
        /**
         * using push() generates a random key in firebase everytime
         */
        donationsDatabase.push().setValue(donation)
    }

    private fun checkForLocationPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestFineLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    private fun requestCoarseLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d("MainActivity", "onRequestPermissionsResult: callback!")

        if (requestCode == 1) {
            if (grantResults.isEmpty() || grantResults == null) {
                // permission not granted
                Log.d("MainActivity", "onRequestPermissionsResult: empty")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // show maps
                Log.d("MainActivity", "onRequestPermissionsResult: permission granted")
                val fragmentManager = supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(
                    R.id.fragment_container,
                    FindHospitalFragment(),
                    nearestHospitalFragmentTag
                )
                transaction.commit()
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // permission not granted
                Log.d("MainActivity", "onRequestPermissionsResult: permission denied")
                Toast.makeText(this, "Please allow location permissions", Toast.LENGTH_LONG).show()
            }
        }
    }
}