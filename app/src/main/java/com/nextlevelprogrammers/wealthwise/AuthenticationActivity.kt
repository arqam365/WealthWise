package com.nextlevelprogrammers.wealthwise

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class AuthenticationActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthenticationUI(
                        onSendVerificationCode = { phoneNumber ->
                            sendVerificationCode(phoneNumber)
                        },
                        onVerifyCode = { code ->
                            verifyCode(code)
                        }
                    )
                }
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to send the verification code to
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout duration
            .setActivity(this)                 // Activity for callbacks
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@AuthenticationActivity, "Verification Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AuthenticationActivity", "Verification Failed", e)
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@AuthenticationActivity.verificationId = verificationId
                    Toast.makeText(this@AuthenticationActivity, "Code Sent", Toast.LENGTH_SHORT).show()
                    Log.d("AuthenticationActivity", "Code Sent: $verificationId")
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId ?: "", code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Authentication Successful", Toast.LENGTH_SHORT).show()
                    // Navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                    Log.e("AuthenticationActivity", "Sign in failed", task.exception)
                }
            }
    }
}