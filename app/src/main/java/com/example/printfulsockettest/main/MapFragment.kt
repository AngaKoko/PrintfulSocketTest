package com.example.printfulsockettest.main

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.printfulsockettest.R
import com.example.printfulsockettest.data.IntentExtras
import com.example.printfulsockettest.data.Users
import com.example.printfulsockettest.data.testListUsers
import com.example.printfulsockettest.databinding.FragmentMapBinding
import com.example.printfulsockettest.sync.FetchAddressIntentService
import com.example.printfulsockettest.utils.bitmapDescriptorFromVector
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener, LocationListener {

    private lateinit var binding: FragmentMapBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var locationManager: LocationManager
    private var mMap: GoogleMap? = null

    private val MIN_TIME: Long = 3000
    private val MIN_DISTANCE = 1000f
    private val PERMISSION_REQUEST_FINE_LOCATION = 201

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_map, container, false)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        // Set callback on the fragment
        mapFragment.getMapAsync(this)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = ViewModelFactory(requireActivity(), application)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(MainViewModel::class.java)

        //location manager to get system current location
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_FINE_LOCATION
            )
        } else {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME,
                    MIN_DISTANCE,
                    this
            ) //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER
        }

        //observe change in list user live data
        viewModel.getListUsers().observe(requireActivity(), Observer {listUser ->
            //return if list of user is empty
            if(listUser.isNullOrEmpty()) return@Observer
            //add user to marker on map if list is not empty
            listUser.forEachIndexed { index, user ->
                user.position = index
                addMarker(user, R.drawable.ic_person_pin)
            }
        })

        viewModel.getNavigateToPosition().observe(requireActivity(), Observer {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, 15f)
            //animate camera to position
            mMap?.animateCamera(cameraUpdate)
        })

        viewModel.getSelectedUser().observe(requireActivity(), Observer {
            //if no address from LatLng, get user address from AddressIntent Service
            if(TextUtils.isEmpty(it.address))
                getAddressFromIntent(LatLng(it.latitude, it.longitude), it.position)
        })

        binding.closeInfoButton.setOnClickListener {
            viewModel.setInfoBubbleVisibility(false)
        }

        binding.nextButton.setOnClickListener {
            val user = viewModel.getSelectedUser().value ?: Users()
            viewModel.nextMarker(user.position)
        }

        binding.previousButton.setOnClickListener {
            val user = viewModel.getSelectedUser().value ?: Users()
            viewModel.previousMarker(user.position)
        }

        binding.lifecycleOwner = requireActivity()
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onMapReady(map: GoogleMap?) {
        mMap = map

        mMap?.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener)
        mMap?.setOnMyLocationClickListener(onMyLocationClickListener)
        enableMyLocationIfPermitted()

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        //clusterManager = ClusterManager(requireContext(), mMap)

        //set list user with test list
        viewModel.setListUsers(testListUsers)

        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.setOnMarkerClickListener(this)
        mMap?.setOnMapClickListener(this)
    }

    //Add marker to map
    private fun addMarker(item: Users, resourceId: Int){
        //return if user has navigate out of fragment
        if (this.activity == null) return
        mMap?.addMarker(
                MarkerOptions()
                        .icon(
                                bitmapDescriptorFromVector(
                                        requireActivity().applicationContext,
                                        resourceId
                                )
                        )
                        .position(LatLng(item.latitude, item.longitude))
        )?.tag = item
    }

    private fun getAddressFromIntent(latLng: LatLng, origin: Int){
        //start address intent service to get address from lat lng
        Intent(requireActivity(), FetchAddressIntentService::class.java).also { intent ->
            intent.putExtra(IntentExtras.LATITUDE, latLng.latitude)
            intent.putExtra(IntentExtras.LONGITUDE, latLng.longitude)
            intent.putExtra(IntentExtras.TAG, origin)
            requireActivity().startService(intent)
        }
    }

    /**
     *Show little details of post when marker is clicked
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        try {
            val user = marker.tag as Users
            viewModel.setSelectedUser(marker.tag as Users)
            viewModel.setInfoBubbleVisibility(true)
            Log.d("marker_object", "Marker = $user")
        }catch (e: Exception){
            Log.d("marker_object", "Error getting user ", e)
        }
        return false
    }

    //Hide user info on map clicked
    override fun onMapClick(p0: LatLng?) {
        viewModel.setInfoBubbleVisibility(false)
        return
    }

    /**
     * Check is location permission and is permitted, enable location
     */
    private fun enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_FINE_LOCATION
            )
        } else if (mMap != null) {
            mMap?.isMyLocationEnabled = true
        }
    }

    /**
     * Show default permission is user does not grant permission
     */
    private fun showDefaultLocation() {
        Toast.makeText(
                requireActivity().applicationContext, "Location permission not granted",
                Toast.LENGTH_SHORT
        ).show()
        val redmond = LatLng(47.6739881, -122.121512)
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(redmond))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    enableMyLocationIfPermitted()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showDefaultLocation()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    /**
     *
     */
    private val onMyLocationButtonClickListener = GoogleMap.OnMyLocationButtonClickListener {
        //mMap.setMinZoomPreference(15);
        statusCheck()
        false
    }

    /**
     *
     */
    private val onMyLocationClickListener = GoogleMap.OnMyLocationClickListener {
        /*
                    mMap.setMinZoomPreference(12);
                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(new LatLng(location.getLatitude(),
                            location.getLongitude()));
                    circleOptions.radius(200);
                    circleOptions.fillColor(Color.RED);
                    circleOptions.strokeWidth(6);
                    mMap.addCircle(circleOptions);
                    */
    }

    /**
     * Check if gps in enabled
     * If not enables build ans alert message
     */
    private fun statusCheck() {
        val manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()

        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton(
                        "Yes"
                ) { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    /**
     * What happens when location is changed -> OnLocationChanged
     */
    override fun onLocationChanged(location: Location?) {
        //return if location is null
        val l = location ?: return
        //Move camera to current location
        val latLng = LatLng(l.latitude, l.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)

        //animate camera to position
        mMap?.animateCamera(cameraUpdate)
        locationManager.removeUpdates(this)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        //nothing to do here
    }

    override fun onProviderEnabled(provider: String?) {
        //nothing to do here
    }

    override fun onProviderDisabled(provider: String?) {
        //nothing to do here
    }

}