package com.example.blooddonation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.blooddonation.R
import com.example.blooddonation.models.Donation
import com.example.blooddonation.viewmodels.DonationViewModel

class DonateBloodFragment : Fragment() {

    private lateinit var incrementButton: Button
    private lateinit var decrementButton: Button
    private lateinit var confirmDonationButton: Button
    private lateinit var unitTextView: TextView
    private lateinit var bloodGroupDonationInfoTextView: TextView
    private lateinit var donationViewModel: DonationViewModel

    private var numberOfUnitsDonated: Int = 0
    private lateinit var username: String
    private lateinit var userBloodGroup: String
    private lateinit var userEmail: String
    private lateinit var userPhoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString("username").toString()
        userBloodGroup = arguments?.getString("bloodGroup").toString()
        userEmail = arguments?.getString("email").toString()
        userPhoneNumber = arguments?.getString("phoneNumber").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_donate_blood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incrementButton = view.findViewById(R.id.increment_blood_unit_button)
        decrementButton = view.findViewById(R.id.decrement_blood_unit_button)
        unitTextView = view.findViewById(R.id.units_donated)
        bloodGroupDonationInfoTextView = view.findViewById(R.id.donate_blood_blood_group)
        confirmDonationButton = view.findViewById(R.id.confirm_button)
        donationViewModel = ViewModelProvider(requireActivity()).get(DonationViewModel::class.java)

        bloodGroupDonationInfoTextView.text =
            resources.getString(R.string.blood_group_information, userBloodGroup)

        incrementButton.setOnClickListener {
            if (numberOfUnitsDonated == 10) {
                Toast.makeText(
                    activity,
                    "You can not donate blood more than 10 units",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            numberOfUnitsDonated++
            unitTextView.text = resources.getString(R.string.donated_units, numberOfUnitsDonated)
        }

        decrementButton.setOnClickListener {
            if (numberOfUnitsDonated == 0) {
                return@setOnClickListener
            }
            numberOfUnitsDonated--
            unitTextView.text = resources.getString(R.string.donated_units, numberOfUnitsDonated)
        }

        confirmDonationButton.setOnClickListener {
            if (numberOfUnitsDonated == 0) {
                Toast.makeText(activity, "You can not donate 0 units", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val donationModel =
                Donation(username, userEmail, userPhoneNumber, userBloodGroup, numberOfUnitsDonated)
            donationViewModel.donationModel.value = donationModel
        }
    }
}