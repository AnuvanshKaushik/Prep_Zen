package com.prepzen.app.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.prepzen.app.R
import com.prepzen.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        setupBottomNavigation(navController)

        requestNotificationPermissionIfNeeded()
    }

    private fun setupBottomNavigation(navController: NavController) {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val target = item.itemId
            val current = navController.currentDestination?.id
            if (current == target) return@setOnItemSelectedListener true

            val options = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(R.id.homeFragment, false, saveState = true)
                .build()

            return@setOnItemSelectedListener runCatching {
                navController.navigate(target, null, options)
                true
            }.getOrDefault(false)
        }

        binding.bottomNav.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.homeFragment) {
                navController.popBackStack(R.id.homeFragment, false)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val inMenu = binding.bottomNav.menu.findItem(destination.id) != null
            if (inMenu) {
                binding.bottomNav.menu.forEach { menuItem ->
                    menuItem.isChecked = menuItem.itemId == destination.id
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
