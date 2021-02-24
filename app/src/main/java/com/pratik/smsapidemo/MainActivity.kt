package com.pratik.smsapidemo

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.pratik.smsapidemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SMS_USER_CONSENT"
        const val REQ_USER_CONSENT = 100
    }

    lateinit var binding: ActivityMainBinding
    lateinit var smsBroadcastReceiver: SmsBroadcastReceiver
    lateinit var etVerificationCode: EditText
    private val CREDENTIAL_PICKER_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        etVerificationCode = binding.filledTextField.editText!!
        setContentView(view)
        startSmsUserConsent()
    }


    override fun onStart() {
        super.onStart()
        registerToSmsBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }

    private fun registerToSmsBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver().also {
            it.smsBroadcastReceiverListener =
                object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                    override fun onSuccess(intent: Intent?) {
                        intent?.let { context -> startActivityForResult(context, REQ_USER_CONSENT) }
                    }

                    override fun onFailure() {
                    }
                }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }


    private fun fetchVerificationCode(message: String): String {
        return Regex("(\\d{6})").find(message)?.value ?: ""
    }


    private fun startSmsUserConsent() {
        SmsRetriever.getClient(this).also {
            //We can add user phone number or leave it blank
            it.startSmsUserConsent(null)
                .addOnSuccessListener {
                    Log.d(TAG, "LISTENING_SUCCESS")
                }
                .addOnFailureListener {
                    Log.d(TAG, "LISTENING_FAILURE")
                }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQ_USER_CONSENT -> {
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    //That gives all message to us. We need to get the code from inside with regex
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    val code = message?.let { fetchVerificationCode(it) }
                    etVerificationCode.setText(code)
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

}