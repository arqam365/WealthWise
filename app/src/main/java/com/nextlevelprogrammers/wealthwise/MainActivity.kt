package com.nextlevelprogrammers.wealthwise

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.nextlevelprogrammers.wealthwise.ui.theme.WealthWiseTheme

class MainActivity : ComponentActivity() {

    private val SMS_PERMISSION_CODE = 101

    // Registering permission launcher to handle the permission request result
    private val requestSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val areAllPermissionsGranted = permissions.values.all { it == true }
        if (areAllPermissionsGranted) {
            Log.d("MainActivity", "All SMS permissions granted")
            // Add any logic if needed when permission is granted
        } else {
            Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
            Log.d("MainActivity", "SMS permissions denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        checkSmsPermission()

        setContent {
            WealthWiseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // Function to check SMS permissions
    private fun checkSmsPermission() {
        if (hasSmsPermissions().not()) {
            // Request permissions using the launcher
            requestSmsPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS
                )
            )
        } else {
            Log.d("MainActivity", "SMS permissions already granted")
        }
    }

    // Helper function to check if SMS permissions are already granted
    private fun hasSmsPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}