package com.lightphone.rcsmessages

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

/**
 * The RCS row model — shaped after chats' `ThreadRowValues` projection
 * (ThreadRowLogic.kt) so the ported screen layer keeps working when the
 * engine layer replaces this in-memory stub. Fields:
 *  - id: RCS message id (eventId analogue)
 *  - isMine: outgoing vs incoming (direction)
 *  - sendStatus: null for incoming; outgoing: SENDING / SENT / DELIVERED /
 *    DISPLAYED / FAILED (IMDN states)
 */
data class RcsRow(
    val id: String,
    val body: String,
    val timestampMs: Long,
    val isMine: Boolean,
    val sendStatus: String? = null,
)

data class RcsThread(
    val address: String,
    val contactName: String,
    val rows: MutableList<RcsRow> = mutableStateListOf(),
) {
    val lastRow: RcsRow? get() = rows.maxByOrNull { it.timestampMs }
}

/**
 * Stub repository: seed data only. The RCS engine layer (registration state,
 * UCE capability exchange, SipDelegate messaging) replaces this object's
 * internals behind the same API — see RCS-LP3-PLAN.md §Carrier path.
 */
object RcsRepository {
    val registrationState = mutableStateOf("NOT_REGISTERED")

    val threads = mutableStateListOf(
        RcsThread("+4915112345678", "Anna").apply {
            rows.addAll(
                listOf(
                    RcsRow("m1", "Hey, are we still on for tomorrow?", now(-35), false),
                    RcsRow("m2", "Yes! Coffee at 10?", now(-33), true, "DELIVERED"),
                    RcsRow("m3", "Perfect, see you there", now(-31), false),
                )
            )
        },
        RcsThread("+4915187654321", "Benni").apply {
            rows.addAll(
                listOf(
                    RcsRow("m4", "Sent you the photos", now(-600), false),
                    RcsRow("m5", "Thanks, looks great!", now(-590), true, "READ"),
                )
            )
        },
    )

    fun send(thread: RcsThread, body: String) {
        thread.rows.add(RcsRow("s${System.currentTimeMillis()}", body, now(0), true, "SENT"))
    }

    private fun now(offsetMinutes: Long): Long =
        System.currentTimeMillis() + offsetMinutes * 60_000
}
