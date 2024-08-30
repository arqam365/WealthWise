package com.nextlevelprogrammers.wealthwise

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle: Bundle? = intent.extras
            if (bundle != null) {
                try {
                    val pdus = bundle.get("pdus") as Array<*>
                    for (pdu in pdus) {
                        val format = bundle.getString("format")
                        val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray, format)

                        val messageBody = smsMessage.messageBody
                        val messageTime = smsMessage.timestampMillis
                        Log.d("SMSReceiver", "Message: $messageBody, Time: $messageTime")

                        // Extract Transaction Details
                        val transactionDetails = extractTransactionDetails(messageBody, messageTime)
                        if (transactionDetails != null) {
                            storeInFirestore(transactionDetails)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SMSReceiver", "Error processing SMS: ${e.message}", e)
                }
            }
        }
    }

    private fun extractTransactionDetails(messageBody: String, messageTime: Long): Map<String, Any>? {
        // Define regex patterns to match both SMS formats
        val transactionIdPattern = Pattern.compile("(?i)Txn\\s*ID[:\\s]*([A-Za-z0-9]+)|(?i)Transaction\\s*ID[:\\s]*([A-Za-z0-9]+)")
        val transactionAmountPattern = Pattern.compile("(?i)INR\\s*([\\d,]+\\.\\d{2})")
        val transactionDateTimePattern = Pattern.compile("(?i)on\\s*(\\d{2}-\\d{2}-\\d{4})\\s*at\\s*(\\d{2}:\\d{2}(?::\\d{2})?)")

        // Extract Transaction ID
        val transactionIdMatcher = transactionIdPattern.matcher(messageBody)
        val transactionId = if (transactionIdMatcher.find()) {
            transactionIdMatcher.group(1) ?: transactionIdMatcher.group(2) ?: ""
        } else ""

        // Extract Transaction Amount
        val transactionAmountMatcher = transactionAmountPattern.matcher(messageBody)
        val transactionAmount = if (transactionAmountMatcher.find()) transactionAmountMatcher.group(1) else ""

        // Extract Transaction Date and Time
        val transactionDateTimeMatcher = transactionDateTimePattern.matcher(messageBody)
        val transactionDateTime = if (transactionDateTimeMatcher.find()) {
            "${transactionDateTimeMatcher.group(1)} ${transactionDateTimeMatcher.group(2)}"
        } else {
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(messageTime))
        }

        // Log extracted details
        Log.d("SMSReceiver", "Extracted - ID: $transactionId, Amount: $transactionAmount, DateTime: $transactionDateTime")

        // Check if essential data is extracted
        return if (transactionId.isNotEmpty() && transactionAmount.isNotEmpty()) {
            mapOf(
                "transactionId" to transactionId,
                "transactionDate" to transactionDateTime,
                "transactionAmount" to transactionAmount,
                "messageTime" to messageTime
            )
        } else {
            Log.e("SMSReceiver", "Failed to extract necessary transaction details from SMS")
            null
        }
    }

    private fun storeInFirestore(transactionDetails: Map<String, Any>) {
        // Get instance of Firestore
        val db = FirebaseFirestore.getInstance()

        // Specify the collection path and document structure
        db.collection("payments")
            .add(transactionDetails)
            .addOnSuccessListener { documentReference ->
                Log.d("SMSReceiver", "Transaction successfully written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("SMSReceiver", "Error writing transaction to Firestore", e)
            }
    }
}
