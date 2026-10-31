package org.fossify.documents.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.documents.R
import org.fossify.documents.models.DocumentEntry
import org.fossify.documents.models.MSyncStatus

/**
 * One line above a folder's contents when m-sync syncs that folder (it left `.m-sync.json`
 * there): when it last synced, what's waiting, or which conflict copy to look at. With a
 * conflict, tapping the strip opens the first conflict copy.
 */
@Composable
internal fun MSyncStatusStrip(
    status: MSyncStatus,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val needsLook = status.state == MSyncStatus.State.CONFLICT || status.state == MSyncStatus.State.ERROR
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = when {
            needsLook -> SimpleTheme.colorScheme.errorContainer
            status.state == MSyncStatus.State.OFFLINE -> SimpleTheme.colorScheme.secondaryContainer
            else -> SimpleTheme.colorScheme.surfaceVariant
        },
        contentColor = when {
            needsLook -> SimpleTheme.colorScheme.onErrorContainer
            status.state == MSyncStatus.State.OFFLINE -> SimpleTheme.colorScheme.onSecondaryContainer
            else -> SimpleTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Row(
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = status.state.icon(),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = status.summary(),
                style = SimpleTheme.typography.bodyMedium,
            )
        }
    }
}

private fun MSyncStatus.State.icon(): ImageVector = when (this) {
    MSyncStatus.State.OK -> Icons.Outlined.CloudDone
    MSyncStatus.State.OFFLINE -> Icons.Outlined.CloudOff
    MSyncStatus.State.CONFLICT -> Icons.Outlined.Warning
    MSyncStatus.State.ERROR -> Icons.Outlined.ErrorOutline
}

@Composable
private fun MSyncStatus.summary(): String {
    val synced = syncedAt?.let {
        DateUtils.getRelativeTimeSpanString(it, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
    }
    return when (state) {
        MSyncStatus.State.OK -> if (synced != null) {
            stringResource(R.string.m_sync_synced_at, synced)
        } else {
            stringResource(R.string.m_sync_synced)
        }

        MSyncStatus.State.OFFLINE -> listOfNotNull(
            if (waiting > 0) {
                pluralStringResource(R.plurals.m_sync_offline_waiting, waiting, waiting)
            } else {
                stringResource(R.string.m_sync_offline)
            },
            synced?.let { stringResource(R.string.m_sync_last_synced, it) },
        ).joinToString(" · ")

        MSyncStatus.State.CONFLICT -> if (conflicts.size == 1) {
            stringResource(R.string.m_sync_conflict_one, conflicts.first().substringAfterLast('/'))
        } else {
            pluralStringResource(R.plurals.m_sync_conflicts, conflicts.size, conflicts.size)
        }

        MSyncStatus.State.ERROR -> stringResource(R.string.m_sync_problem, message.ifBlank { "?" })
    }
}

/** The meta line under a document's name, marked when it's an m-sync conflict copy. */
@Composable
internal fun DocumentEntry.syncAwareMetaLine(): String {
    val meta = metaLine(showOpenedFallback = true)
    return if (MSyncStatus.isConflictCopy(name)) stringResource(R.string.m_sync_conflict_copy_meta, meta) else meta
}

@Composable
internal fun DocumentEntry.syncAwareMetaColor(alpha: Float): Color {
    return if (MSyncStatus.isConflictCopy(name)) {
        SimpleTheme.colorScheme.error
    } else {
        SimpleTheme.colorScheme.onSurface.copy(alpha = alpha)
    }
}
