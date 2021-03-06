package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.Message
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    val reminderActivity: RemindersActivity by lazy {
        activity as RemindersActivity
    }

    //Use Koin to get the view model of the SaveReminder
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var locationManager: LocationManager

    var locationListener: LocationListener = LocationListener { location ->
        updateLocation(location)
    }

    private fun updateLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        val zoomLevel = 15f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoomLevel))
        locationManager.removeUpdates(locationListener)
    }

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.location_map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

//        TODO: add style to the map

        locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }

        binding.selectBtn.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        viewModel.navigationCommand.value = NavigationCommand.Back
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        enableMyLocation()

        val lat = 37.422160
        val lng = -122.084270
        val latLng = LatLng(lat, lng)

        val zoomLevel = 10f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

        setMapClick(map)
        setMapPoiClick(map)
        setInfoWindowClick(map)
    }

    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            map.clear()
            val snippet = getString(R.string.lat_long_snippet, latLng.latitude, latLng.longitude)
            map.addMarker(
                MarkerOptions().position(latLng).title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            ).apply { showInfoWindow() }
            viewModel.selectedPOI.value = null
            viewModel.latLng.value = latLng
        }
    }

    private fun setMapPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
                .apply { showInfoWindow() }
            viewModel.selectedPOI.value = poi
            viewModel.latLng.value = poi.latLng
        }
    }

    private fun setInfoWindowClick(map: GoogleMap) {
        map.setOnInfoWindowClickListener {
            viewModel.showMessage.value = Message(
                requireContext(),
                getString(R.string.choose_location),
                getString(
                    R.string.choose_location_text,
                    if (viewModel.selectedPOI.value != null) viewModel.selectedPOI.value?.name
                        ?: "" else getString(
                        R.string.lat_long_snippet,
                        viewModel.latLng.value?.latitude,
                        viewModel.latLng.value?.longitude
                    )
                ),
                Pair(getString(R.string.choose_button), DialogInterface.OnClickListener { _, _ ->
                    onLocationSelected()
                }),
                negativeButton = Pair(
                    getString(R.string.cancel_button),
                    DialogInterface.OnClickListener { dialog, _ -> dialog.dismiss() })
            )
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true

            try {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
            } catch (_: Exception) {
            }
        } else {
            reminderActivity.permissionCallback = { enableMyLocation() }
            reminderActivity.requestPermission()
        }
    }

    private fun onBackPressed() {
        viewModel.selectedPOI.value = null
        viewModel.latLng.value = null
        viewModel.showToast.value = getString(R.string.select_cancelled)
        viewModel.navigationCommand.value = NavigationCommand.Back
    }
}
