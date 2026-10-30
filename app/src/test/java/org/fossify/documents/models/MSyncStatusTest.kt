package org.fossify.documents.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MSyncStatusTest {
    @Test
    fun `reads the states m-sync writes`() {
        assertEquals(MSyncStatus.State.OK, MSyncStatus.from("ok")?.state)
        assertEquals(MSyncStatus.State.OFFLINE, MSyncStatus.from("offline")?.state)
        assertEquals(MSyncStatus.State.CONFLICT, MSyncStatus.from("conflict")?.state)
        assertEquals(MSyncStatus.State.ERROR, MSyncStatus.from("ERROR")?.state)
        assertNull(MSyncStatus.from("syncing"))
        assertNull(MSyncStatus.from(""))
    }

    @Test
    fun `parses m-sync timestamps with their offset`() {
        val withOffset = MSyncStatus.parseTime("2026-10-30T10:56:15-0400")
        val utc = MSyncStatus.parseTime("2026-10-30T14:56:15+0000")
        assertEquals(utc, withOffset)
        assertEquals(1_793_372_175_000L, utc)
        assertNull(MSyncStatus.parseTime(null))
        assertNull(MSyncStatus.parseTime("yesterday"))
    }

    @Test
    fun `keeps counts and conflict names sane`() {
        val status = MSyncStatus.from(
            state = "conflict",
            synced = "2026-10-30T10:56:15-0400",
            waiting = -3,
            conflicts = listOf("plan.conflict-s5.md", ""),
        )
        assertEquals(0, status?.waiting)
        assertEquals(listOf("plan.conflict-s5.md"), status?.conflicts)
        assertEquals(MSyncStatus.parseTime("2026-10-30T10:56:15-0400"), status?.syncedAt)
        assertNull(status?.checkedAt)
    }

    @Test
    fun `recognizes conflict copies by name`() {
        assertTrue(MSyncStatus.isConflictCopy("plan.conflict-s5.md"))
        assertTrue(MSyncStatus.isConflictCopy("plan.conflict-s5-2.md"))
        assertTrue(MSyncStatus.isConflictCopy("todo.conflict-yoga"))
        assertFalse(MSyncStatus.isConflictCopy("conflict-resolution.md"))
        assertFalse(MSyncStatus.isConflictCopy(".conflict-s5.md"))
        assertFalse(MSyncStatus.isConflictCopy("plan.conflict-"))
        assertFalse(MSyncStatus.isConflictCopy("plan.md"))
    }
}
