package com.example.blooddonation

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class DonorDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor_details)

        // adding back button to action bar
        val actionBar = supportActionBar
        actionBar!!.title = "Donor Contact"
        actionBar.setDisplayHomeAsUpEnabled(true)

        val donorNameTextView: TextView = findViewById(R.id.donor_name)
        val donorEmailTextView: TextView = findViewById(R.id.donor_email)
        val donorPhoneNumberTextView: TextView = findViewById(R.id.donor_phone)
        val donorBloodGroupTextView: TextView = findViewById(R.id.blood_group)
        val numberOfBloodUnitsTextView: TextView = findViewById(R.id.number_of_units_available)
        val makePhoneCallButton: Button = findViewById(R.id.make_call)

        val donorName: String = intent.extras?.getString("donor_name").toString()
        val donorBloodGroup: String = intent.extras?.getString("donor_blood_group").toString()
        val numberOfUnitsDonated: Int = intent.extras?.getInt("number_of_units")!!.toInt()
        val donorPhoneNumber: String = intent.extras?.getString("donor_phone").toString()
        val donorEmail: String = intent.extras?.getString("donor_email").toString()

        donorNameTextView.text = donorName
        donorEmailTextView.text = donorEmail
        donorPhoneNumberTextView.text = donorPhoneNumber
        donorBloodGroupTextView.text =
            resources.getString(R.string.blood_group_info_donor_detail, donorBloodGroup)
        numberOfBloodUnitsTextView.text =
            resources.getString(R.string.number_of_units_available, numberOfUnitsDonated)

        makePhoneCallButton.setOnClickListener {
            // check for permission to make a phone call
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CALL_PHONE),
                    1
                )
            } else {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$donorPhoneNumber"))
                startActivity(intent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()     //Called when the activity has detected the user's press of the back key.
        return true
    }
}