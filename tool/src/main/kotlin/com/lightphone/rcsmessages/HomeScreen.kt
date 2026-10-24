package com.lightphone.rcsmessages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.lightClickable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Thread list — ported from chats' ChatListScreen pattern. */
@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) :
    SimpleLightScreen<Unit>(sealedActivity) {

    @Composable
    override fun Content() {
        Column(modifier = Modifier.fillMaxWidth()) {
            LightText(
                text = "RCS Messages",
                variant = LightTextVariant.Heading,
                modifier = Modifier.padding(16.dp),
            )
            LazyColumn {
                items(RcsRepository.threads) { thread ->
                    ThreadListRow(thread) {
                        navigateTo(screenFactory = { ThreadScreen(it, thread) })
                    }
                }
            }
        }
    }
}

private fun formatTime(ts: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))

@Composable
private fun ThreadListRow(thread: RcsThread, onClick: () -> Unit) {
    val last = thread.lastRow
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .lightClickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LightText(
                text = thread.contactName,
                variant = LightTextVariant.Copy,
                modifier = Modifier.weight(1f),
            )
            if (last != null) {
                LightText(
                    text = formatTime(last.timestampMs),
                    variant = LightTextVariant.Detail,
                )
            }
        }
        if (last != null) {
            val preview = if (last.isMine) "You: ${last.body}" else last.body
            LightText(
                text = preview,
                variant = LightTextVariant.Detail,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
