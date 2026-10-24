package com.lightphone.rcsprobe

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/** Stub SMS role-qualification components — no real SMS handling. */
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "RcsProbe: SMS received (stub)", Toast.LENGTH_SHORT).show()
    }
}

class WapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "RcsProbe: WAP push received (stub)", Toast.LENGTH_SHORT).show()
    }
}

class SmsSentActivity : android.app.Activity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}

class RespondViaMessageService : android.app.Service() {
    override fun onBind(intent: Intent?) = null
}
