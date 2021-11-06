package com.example.blooddonation.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blooddonation.DonorDetailsActivity
import com.example.blooddonation.R
import com.example.blooddonation.models.Donation
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class DonorRecyclerAdapter(
    private val context: Context,
    options: FirebaseRecyclerOptions<Donation>,
) :
    FirebaseRecyclerAdapter<Donation, DonorRecyclerAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DonorRecyclerAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.donor_card_layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Donation) {
        holder.donorName.text = model.donorUsername
        holder.donorBloodGroup.text = model.donorBloodGroup
        holder.unitsDonated.text =
            context.resources.getString(R.string.donor_card_view_units, model.unitsDonated)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DonorDetailsActivity::class.java)
            intent.putExtra("donor_name", model.donorUsername)
            intent.putExtra("donor_blood_group", model.donorBloodGroup)
            intent.putExtra("number_of_units", model.unitsDonated)
            intent.putExtra("donor_phone", model.donorPhoneNumber)
            intent.putExtra("donor_email", model.donorEmail)
            context.startActivity(intent)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var donorName: TextView = itemView.findViewById(R.id.donor_name)
        var donorBloodGroup: TextView = itemView.findViewById(R.id.donor_blood_group)
        var unitsDonated: TextView = itemView.findViewById(R.id.units_donated)
    }
}