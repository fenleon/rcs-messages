package com.lightphone.rcsmessages.server

import android.content.Context
import android.telephony.SubscriptionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors
import java.util.function.Consumer

/**
 * RCS engine bridge — registration state via the framework ImsRcsManager
 * surface (reflection; the access map is in LIGHT-FINDINGS.md). This is the
 * seam that will grow into the full engine (UCE, SipDelegate messaging)
 * behind chats' existing interfaces.
 *
 * States: NOT_REGISTERED / REGISTERING / REGISTERED / ACCESS_BLOCKED /
 * NO_SUBSCRIPTION.
 */
object RcsEngine {
    private val _registrationState = MutableStateFlow("UNKNOWN")
    val registrationState: StateFlow<String> get() = _registrationState

    fun start(context: Context) {
        if (_registrationState.value != "UNKNOWN") return
        try {
            val subIds = try {
                val sm = context.getSystemService(SubscriptionManager::class.java)
                (sm?.activeSubscriptionInfoList ?: emptyList()).map { it.subscriptionId }
            } catch (_: Throwable) {
                emptyList()
            }
            if (subIds.isEmpty()) {
                _registrationState.value = "NO_SUBSCRIPTION"
                return
            }
            val imsMgrCls = Class.forName("android.telephony.ims.ImsManager")
            val mgr = context.getSystemService(imsMgrCls)
                ?: run { _registrationState.value = "UNKNOWN"; return }
            val getRcs = imsMgrCls.methods.first { it.name == "getImsRcsManager" }
            val rcs = getRcs.invoke(mgr, subIds[0])
            val reg = rcs.javaClass.methods.first { it.name == "getRegistrationState" }
            val executor = Executors.newSingleThreadExecutor()
            val consumer = Consumer<Int> { s ->
                _registrationState.value = when (s) {
                    0 -> "NOT_REGISTERED"
                    1 -> "REGISTERING"
                    2 -> "REGISTERED"
                    else -> "STATE_$s"
                }
            }
            reg.invoke(rcs, executor, consumer)
        } catch (t: Throwable) {
            _registrationState.value = "ACCESS_BLOCKED"
        }
    }
}
