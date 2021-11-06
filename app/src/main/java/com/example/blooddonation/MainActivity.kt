package com.example.blooddonation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.blooddonation.fragments.DonateBloodFragment
import com.example.blooddonation.fragments.FindDonorFragment
import com.example.blooddonation.fragments.HomeFragment
import com.example.blooddonation.fragments.HospitalFragment
import com.example.blooddonation.models.Donation
import com.example.blooddonation.utils.ProgressDialog
import com.example.blooddonation.viewmodels.DonationViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var bloodGroup: String
    private lateinit var phoneNumber: String

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

        donationViewModel = ViewModelProvider(this).get(DonationViewModel::class.java)
        mAuth = FirebaseAuth.getInstance()
        userDatabase = FirebaseDatabase.getInstance().getReference("users")
        donationsDatabase = FirebaseDatabase.getInstance().getReference("donations")

        navigationView = findViewById(R.id.navigation_view)
        val headerView: View = navigationView.getHeaderView(0)
        val navDrawerHeaderUsernameTextView: TextView = headerView.findViewById(R.id.username)
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
        navigationView.setNavigationItemSelectedListener(this)

        val fragment = supportFragmentManager.beginTransaction()
        fragment.replace(R.id.fragment_container, HomeFragment()).commit()
        setToolBarTitle("Home")

        userId = mAuth.uid.toString()

        val progressDialog = ProgressDialog.progressDialog(this)
        progressDialog.show()
        userDatabase.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                username = snapshot.child("username").value.toString()
                email = snapshot.child("email").value.toString()
                bloodGroup = snapshot.child("bloodGroup").value.toString()
                phoneNumber = snapshot.child("phoneNumber").value.toString()
                Log.d("On data get", "onDataChange: $username $email $bloodGroup $phoneNumber")
                navDrawerHeaderUsernameTextView.text = username
                progressDialog.hide()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

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

        val user = mAuth.currentUser
        if (user == null) {
            //there is no user logged in
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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

                if (supportFragmentManager.findFragmentByTag(nearestHospitalFragmentTag)?.isVisible == true) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return false
                }

                val fragmentManager = supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(
                    R.id.fragment_container,
                    HospitalFragment(),
                    nearestHospitalFragmentTag
                )
                transaction.commit()
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
}