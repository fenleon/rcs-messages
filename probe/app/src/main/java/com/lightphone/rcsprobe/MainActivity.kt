package com.lightphone.rcsprobe

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.ScrollView
import android.widget.TextView

/**
 * C0 probe: enumerate the AOSP RCS client surface (ImsRcsManager, RcsUceAdapter,
 * SipDelegateManager, ProvisioningManager) as seen from a normal sideloaded app,
 * then attempt the actual calls. Everything lands in logcat tag `RcsProbe`.
 */
class MainActivity : Activity() {
    private val out = StringBuilder()
    private fun line(s: String) { out.append(s).append('\n'); Log.i("RcsProbe", s) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_PHONE_STATE), 1)
        } else {
            probe()
        }
        setContentView(ScrollView(this).apply {
            addView(TextView(this@MainActivity).apply {
                text = out; setPadding(24, 24, 24, 24); setTextIsSelectable(true)
            })
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        out.append('\n')
        probe()
        setContentView(ScrollView(this).apply {
            addView(TextView(this@MainActivity).apply {
                text = out; setPadding(24, 24, 24, 24); setTextIsSelectable(true)
            })
        })
    }

    private fun cls(name: String): Class<*>? = try {
        Class.forName(name)
    } catch (t: Throwable) {
        line("CLASS $name: NOT RESOLVABLE")
        null
    }

    private fun dumpMethods(c: Class<*>) {
        line("--- ${c.name} declared public surface ---")
        c.methods.map { m ->
            val params = m.parameterTypes.joinToString(",") { it.simpleName }
            "  ${m.returnType.simpleName} ${m.name}($params)"
        }.sorted().forEach { line(it) }
    }

    private fun probe() {
        line("device=${android.os.Build.FINGERPRINT} sdk=${android.os.Build.VERSION.SDK_INT}")

        // 1. subscriptions
        val subIds: List<Int> = try {
            val sm = getSystemService(SubscriptionManager::class.java)
            val list = sm.activeSubscriptionInfoList
            val ids = (list ?: emptyList()).map { it.subscriptionId }
            line("subscriptionIds=$ids")
            ids
        } catch (t: Throwable) {
            line("subscriptionIds FAILED: $t")
            emptyList()
        }

        // 2. class presence
        val imsRcs = cls("android.telephony.ims.ImsRcsManager")
        val uce = cls("android.telephony.ims.RcsUceAdapter")
        val sipDel = cls("android.telephony.ims.SipDelegateManager")
        val prov = cls("android.telephony.ims.ProvisioningManager")
        val imsMgr = cls("android.telephony.ims.ImsManager")

        // 3. system service acquisition attempts
        for (svc in listOf("ims_rcs", "ipms", "sipdelegate")) {
            try {
                val o = getSystemService(svc)
                line("getSystemService(\"$svc\") -> ${o?.javaClass?.name ?: "null"}")
            } catch (t: Throwable) {
                line("getSystemService(\"$svc\") FAILED: $t")
            }
        }
        if (imsRcs != null) {
            try {
                val o = getSystemService(imsRcs)
                line("getSystemService(ImsRcsManager.class) -> ${o?.javaClass?.name ?: "null"}")
                if (o != null) subIds.forEach { attemptRegistrationState(o, it) }
            } catch (t: Throwable) {
                line("getSystemService(ImsRcsManager.class) FAILED: $t")
            }
        }

        // 4. per-subscription manager attempts (AOSP: ImsManager.getImsRcsManager(subId))
        if (imsMgr != null && subIds.isNotEmpty()) {
            try {
                val mgr = getSystemService(imsMgr)
                if (mgr != null) {
                    for (subId in subIds) {
                        for (m in imsMgr.methods.filter { it.name.contains("Rcs", true) }) {
                            try {
                                val r = if (m.parameterTypes.size == 1) m.invoke(mgr, subId) else m.invoke(mgr)
                                line("ImsManager.${m.name}($subId) -> $r")
                                if (imsRcs != null && r != null && imsRcs.isInstance(r)) {
                                    attemptRegistrationState(r, subId)
                                    attemptUce(r)
                                }
                            } catch (t: Throwable) {
                                line("ImsManager.${m.name}($subId) FAILED: ${t.cause ?: t}")
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                line("ImsManager getSystemService FAILED: $t")
            }
        }

        // 5. ProvisioningManager.createForSubscriptionId (C2-relevant)
        if (prov != null && subIds.isNotEmpty()) {
            try {
                val create = prov.methods.first { it.name == "createForSubscriptionId" }
                val pm = create.invoke(null, subIds[0])
                line("ProvisioningManager.createForSubscriptionId(${subIds[0]}) -> $pm")
                for (m in prov.methods.filter {
                    it.parameterTypes.isEmpty() && it.returnType == Boolean::class.javaPrimitiveType &&
                        (it.name.contains("Provision", true) || it.name.contains("Capable", true))
                }) {
                    try { line("ProvisioningManager.${m.name}() -> ${m.invoke(pm)}") }
                    catch (t: Throwable) { line("ProvisioningManager.${m.name}() FAILED: ${t.cause ?: t}") }
                }
                // C2 experiment: can a platform-signed app WRITE provisioning state?
                for (cap in 0..3) {
                    try {
                        val get = prov.methods.first { it.name == "getRcsProvisioningStatusForCapability" }
                        line("getRcsProvisioningStatusForCapability($cap) -> ${get.invoke(pm, cap)}")
                    } catch (t: Throwable) {
                        line("getRcsProvisioningStatusForCapability($cap) FAILED: ${t.cause ?: t}")
                    }
                    try {
                        val set = prov.methods.first { it.name == "setRcsProvisioningStatusForCapability" }
                        set.invoke(pm, cap, true)
                        line("setRcsProvisioningStatusForCapability($cap, true) invoked OK")
                    } catch (t: Throwable) {
                        line("setRcsProvisioningStatusForCapability($cap, true) FAILED: ${t.cause ?: t}")
                        break
                    }
                }
            } catch (t: Throwable) {
                line("ProvisioningManager.createForSubscriptionId FAILED: ${t.cause ?: t}")
            }
        }

        if (sipDel != null) dumpMethods(sipDel)
        if (uce != null) dumpMethods(uce)
        if (imsRcs != null) dumpMethods(imsRcs)

        line("PROBE DONE")
    }

    private fun attemptRegistrationState(rcsMgr: Any, subId: Int) {
        val m = rcsMgr.javaClass.methods.firstOrNull {
            it.name == "getRegistrationState" || it.name == "getImsRegistrationState"
        } ?: run {
            line("no registration-state method on ${rcsMgr.javaClass.name}")
            return
        }
        val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
        val consumerClass = Class.forName("java.util.function.Consumer")
        val callback = java.lang.reflect.Proxy.newProxyInstance(
            javaClass.classLoader, arrayOf(consumerClass)
        ) { _, method, args ->
            if (method.name == "accept") line("REGISTRATION STATE UPDATE: ${args?.joinToString()}")
            null
        }
        try {
            m.invoke(rcsMgr, executor, callback)
            line("getRegistrationState(subId=$subId) invoked OK — awaiting callback")
        } catch (t: Throwable) {
            line("getRegistrationState(subId=$subId) FAILED: ${t.cause ?: t}")
        }
    }

    private fun attemptUce(rcsMgr: Any) {
        try {
            val uceObj = rcsMgr.javaClass.methods.first { it.name == "getUceAdapter" }.invoke(rcsMgr)
            line("getUceAdapter -> $uceObj")
            val uceCls = uceObj.javaClass
            for (name in listOf("isUceSettingEnabled", "getUcePublishState")) {
                uceCls.methods.firstOrNull { it.name == name }?.let {
                    try { line("UceAdapter.${name}() -> ${it.invoke(uceObj)}") }
                    catch (t: Throwable) { line("UceAdapter.${name}() FAILED: ${t.cause ?: t}") }
                }
            }
            // requestCapabilities(Collection<Uri>, Executor, CapabilitiesCallback) — query own number
            val req = uceCls.methods.firstOrNull { it.name == "requestCapabilities" }
            if (req != null) {
                try {
                    val self = listOf(android.net.Uri.parse("tel:+4915233561404"))
                    val cbClass = req.parameterTypes[2]
                    val cb = java.lang.reflect.Proxy.newProxyInstance(
                        javaClass.classLoader, arrayOf(cbClass)
                    ) { _, method, args ->
                        line("UCE CALLBACK ${method.name}: ${args?.joinToString()}")
                        null
                    }
                    val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
                    req.invoke(uceObj, self, executor, cb)
                    line("requestCapabilities invoked OK — awaiting callback")
                } catch (t: Throwable) {
                    line("requestCapabilities FAILED: ${t.cause ?: t}")
                }
            }
        } catch (t: Throwable) {
            line("UCE path FAILED: ${t.cause ?: t}")
        }
    }
}
