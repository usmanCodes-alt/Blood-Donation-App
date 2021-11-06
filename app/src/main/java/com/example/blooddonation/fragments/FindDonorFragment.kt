package com.example.blooddonation.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blooddonation.R
import com.example.blooddonation.adapters.DonorRecyclerAdapter
import com.example.blooddonation.models.Donation
import com.example.blooddonation.utils.ProgressDialog
import com.example.blooddonation.utils.WrapDonorCardLinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*

class FindDonorFragment : Fragment() {

    private lateinit var donorDatabase: DatabaseReference
    private lateinit var adapter: DonorRecyclerAdapter
    private lateinit var donorRecyclerView: RecyclerView
    private lateinit var allDonations: ArrayList<Donation>
    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog.progressDialog(requireContext())
        progressDialog.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        donorDatabase = FirebaseDatabase.getInstance().getReference("donations")
        allDonations = ArrayList()
        return inflater.inflate(R.layout.fragment_find_donor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        donorRecyclerView = view.findViewById(R.id.donor_recycler_view)
        donorRecyclerView.layoutManager = WrapDonorCardLinearLayoutManager(requireContext())

        val query = donorDatabase.limitToLast(10)

        /**
         * below we are preparing our data from the data source
         */
        val recyclerOptions: FirebaseRecyclerOptions<Donation> =
            FirebaseRecyclerOptions.Builder<Donation>()
                .setQuery(query, Donation::class.java).build()

        adapter = DonorRecyclerAdapter(requireContext(), recyclerOptions)
        donorRecyclerView.adapter = adapter

        query.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.hide()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not implemented")
            }

        })
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.stopListening()
    }
}