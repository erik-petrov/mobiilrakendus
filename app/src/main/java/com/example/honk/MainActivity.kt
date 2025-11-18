package com.example.honk

import LocationViewModel
import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.honk.databinding.ActivityMainBinding
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import kotlin.getValue
import androidx.lifecycle.Observer
// testing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    private val locationViewModel: LocationViewModel by viewModels()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                println("loc perm granted")
            } else {
                println("loc perm NOT granted")
            }
        }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // testing
        val auth1 = FirebaseAuth.getInstance()
        val db1 = FirebaseFirestore.getInstance()


        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.calendarFragment,
                R.id.notesFragment,
                R.id.categoriesFragment,
                R.id.gooseFragment,
                R.id.folderDetailsFragment
            )
        )

        val sharedPreferences = getSharedPreferences("app_theme", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(nightMode)

        binding.navView.setupWithNavController(navController)

        locationViewModel.initialize(applicationContext)

        checkAndRequestPermission(::startLocationUpdates)

        //sample for demo
        locationViewModel.currentLocation.observe(this, Observer { location ->
            println(location)
        })
    }

    private fun checkAndRequestPermission(actionIfGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            actionIfGranted()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startLocationUpdates() {
        locationViewModel.startLocationUpdates()
    }
}
