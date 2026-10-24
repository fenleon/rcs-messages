package com.lightphone.rcsmessages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Conversation view — ported from chats' ThreadScreen pattern: incoming
 * messages left-aligned, outgoing right-aligned with send status
 * (IMDN states), composer stubbed until the engine layer lands.
 */
class ThreadScreen(
    sealedActivity: SealedLightActivity,
    private val thread: RcsThread,
) : SimpleLightScreen<Unit>(sealedActivity) {

    @Composable
    override fun Content() {
        Column(modifier = Modifier.fillMaxSize()) {
            LightText(
                text = thread.contactName,
                variant = LightTextVariant.Heading,
                modifier = Modifier.padding(16.dp),
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(thread.rows) { row -> MessageRow(row) }
            }
            // Composer stub: the real input arrives with the engine layer.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                LightBarButton.LightIcon(
                    icon = LightIcons.SEND,
                    contentDescription = "Send test message",
                    onClick = { RcsRepository.send(thread, "Test message") },
                )
            }
        }
    }
}

@Composable
private fun MessageRow(row: RcsRow) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (row.isMine) Alignment.End else Alignment.Start,
    ) {
        LightText(text = row.body, variant = LightTextVariant.Copy)
        val meta = buildString {
            append(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(row.timestampMs)))
            if (row.isMine && row.sendStatus != null) append(" · ${row.sendStatus}")
        }
        LightText(
            text = meta,
            variant = LightTextVariant.Detail,
            modifier = Modifier.padding(top = 1.dp),
        )
    }
}
