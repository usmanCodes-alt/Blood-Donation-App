package com.example.blooddonation.fragments

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.blooddonation.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FindHospitalFragment : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val callback = OnMapReadyCallback { googleMap ->
        Log.d("FindHospitalFragment", "OnMapReadyCallback: Maps are ready")

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        // check for permission
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//
//            && (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                android.Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED)
//        ) {
            // The permission has been granted
            try {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { p0 ->
                    val userLocation: Location = p0.result
                    val locationToGoToOnMap = LatLng(userLocation.latitude, userLocation.longitude)
                    Log.d("FindHospitalFragment", "OnMapReadyCallback: ${locationToGoToOnMap.latitude}, ${locationToGoToOnMap.longitude}")
                    googleMap.addMarker(
                        MarkerOptions().position(locationToGoToOnMap)
                            .title("Marker on User Location")
                    )
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            locationToGoToOnMap,
                            15f
                        )
                    )
                }
            } catch (e: SecurityException) {
                MaterialAlertDialogBuilder(requireContext()).setMessage("There was a Problem Loading Maps, please try again later.")
                    .setPositiveButton("Ok") { dialog, which ->
                        val fragmentManager = requireActivity().supportFragmentManager
                        val transaction = fragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, HomeFragment()).commit()
                    }
            }
//        }
//        else {
//            Log.d("FindHospitalFragment", "OnMapReadyCallback: Permission not granted")
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_find_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}