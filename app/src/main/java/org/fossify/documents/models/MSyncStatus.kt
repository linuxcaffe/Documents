package org.fossify.documents.models

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * The status file m-sync (tw-mobilize's notebook sync for Termux) leaves in each folder it
 * syncs: `.m-sync.json`, with `state`, `message`, `synced`, `checked`, `waiting` and `conflicts`.
 * The app only reads it; nothing here talks to Termux or git.
 */
data class MSyncStatus(
    val state: State,
    val message: String = "",
    val syncedAt: Long? = null,
    val checkedAt: Long? = null,
    val waiting: Int = 0,
    val conflicts: List<String> = emptyList(),
) {
    enum class State { OK, OFFLINE, CONFLICT, ERROR }

    companion object {
        const val FILE_NAME = ".m-sync.json"
        private const val CONFLICT_MARK = ".conflict-"

        @Suppress("LongParameterList")
        fun from(
            state: String,
            message: String = "",
            synced: String? = null,
            checked: String? = null,
            waiting: Int = 0,
            conflicts: List<String> = emptyList(),
        ): MSyncStatus? {
            val parsedState = when (state.lowercase(Locale.ROOT)) {
                "ok" -> State.OK
                "offline" -> State.OFFLINE
                "conflict" -> State.CONFLICT
                "error" -> State.ERROR
                else -> return null
            }
            return MSyncStatus(
                state = parsedState,
                message = message,
                syncedAt = parseTime(synced),
                checkedAt = parseTime(checked),
                waiting = waiting.coerceAtLeast(0),
                conflicts = conflicts.filter { it.isNotBlank() },
            )
        }

        /** m-sync writes `2026-10-30T10:56:15-0400` (Python's `%Y-%m-%dT%H:%M:%S%z`). */
        fun parseTime(value: String?): Long? {
            if (value.isNullOrBlank()) return null
            return try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ROOT).parse(value)?.time
            } catch (_: ParseException) {
                null
            }
        }

        /** `plan.conflict-s5.md`: the copy m-sync saved when a note changed on two stations. */
        fun isConflictCopy(name: String): Boolean {
            val mark = name.indexOf(CONFLICT_MARK)
            return mark > 0 && mark + CONFLICT_MARK.length < name.length
        }
    }
}
