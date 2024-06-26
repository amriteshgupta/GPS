package com.example.gps

import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gps.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var boolean: Boolean = false
    //Google's API for location Service.
    lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    var locationRequest= LocationRequest.create()
    private lateinit var locationCallback: LocationCallback
    private lateinit var geoCoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationRequest.setInterval(1000*30)
        locationRequest.setFastestInterval(1000*5)
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)

        binding.swGps.setOnClickListener{
            if(binding.swGps.isChecked){
          locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                binding.tvSensor.setText("using GPS sensor")
            }
            else{
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                binding.tvSensor.setText("using tower and wifi")
            }
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations){

                    updateUIValues(locationResult.lastLocation)
                }
            }
        }


        binding.swLocationsupdates.setOnClickListener{
            if (binding.swLocationsupdates.isChecked){
                startLocationUpdates()
            }
            else{
                stopLocationUpdates()
            }
        }
        updateGPS()

    }


    private fun stopLocationUpdates() {
        binding.tvLat.setText("Location updates stopped")
        binding.tvUpdates.setText("Location is not being updated")
        binding.tvAccuracy.setText("Not available")
        binding.tvLon.setText("Not available")
        binding.tvSpeed.setText("Not available")
        binding.tvAltitude.setText("Not available")
        binding.tvSensor.setText("Not available")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdates() {
        binding.tvLat.setText("Location updates started")
        binding.tvUpdates.setText("Location is being updated")
        binding.tvAccuracy.setText("Not available")
        binding.tvLon.setText("Not available")
        binding.tvSpeed.setText("Not available")
        binding.tvAltitude.setText("Not available")
        binding.tvSensor.setText("Not available")

        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        updateGPS()

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            123 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS()
                } else {
                    Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

    }

    fun updateGPS(){
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this,object :
                OnSuccessListener<Location> {
                override fun onSuccess(location: Location?){
                    updateUIValues(location)
                }
            })
        }
        else{
//            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),123)4
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),123)
            }
        }

    }
    fun updateUIValues(location: Location?){
        if(location!=null){
            binding.tvLat.setText(location.latitude.toString())
            binding.tvLon.setText(location.longitude.toString())
            binding.tvAccuracy.setText(location.accuracy.toString())

            if(location.hasAltitude()){
                binding.tvAltitude.setText(location.altitude.toString())
            }
            else{
                binding.tvAltitude.setText("Not available")
            }
            if(location.hasSpeed()){
                binding.tvSpeed.setText(location.speed.toString())
            }
            else{
                binding.tvSpeed.setText("Not available")
            }
        }
        else{
            Toast.makeText(this,"Unable to fetch the location",Toast.LENGTH_SHORT).show()
        }
         geoCoder=Geocoder(this, Locale.getDefault())
        try {
            val addresses=geoCoder.getFromLocation(location!!.latitude,location.longitude,1)
            if (addresses != null) {
                binding.tvAddress.setText(addresses.get(0).getAddressLine(0))
            }
        }
        catch (e:Exception){
            binding.tvAddress.setText("Unable to fetch the address")
            }

    }

}