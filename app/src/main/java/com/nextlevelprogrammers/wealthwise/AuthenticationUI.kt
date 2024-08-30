package com.nextlevelprogrammers.wealthwise

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthenticationUI(
    onSendVerificationCode: (String) -> Unit,
    onVerifyCode: (String) -> Unit,
//    onResendCode: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Enter Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onSendVerificationCode(phoneNumber) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Verification Code")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = verificationCode,
            onValueChange = { verificationCode = it },
            label = { Text("Enter Verification Code") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onVerifyCode(verificationCode) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify Code")
        }

//        Button(
//            onClick = { onResendCode(phoneNumber) },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Resend Code")
//        }
    }
}
