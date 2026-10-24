package com.lightphone.rcsmessages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant

/**
 * RCS Messages — first skeleton screen. The RCS engine layer (registration
 * state, capability exchange, threads) plugs in behind this UI; see
 * RCS-LP3-PLAN.md §Carrier path. UI bootstrapped from the chats tool's
 * design language.
 */
@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) :
    SimpleLightScreen<Unit>(sealedActivity) {

    @Composable
    override fun Content() {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LightText(
                text = "RCS Messages",
                variant = LightTextVariant.Heading,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
