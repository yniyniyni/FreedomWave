package art.yniyniyni.freedomwave.ui.feature.squads

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.domain.model.SquadMember
import art.yniyniyni.freedomwave.domain.model.UserStatus
import art.yniyniyni.freedomwave.ui.components.FwSectionIcon
import art.yniyniyni.freedomwave.ui.l10n.localized
import art.yniyniyni.freedomwave.ui.theme.LocalFwStatus
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_retry
import freedomwave.composeapp.generated.resources.squads_edit_members
import freedomwave.composeapp.generated.resources.squads_members_empty
import freedomwave.composeapp.generated.resources.squads_members_showing
import org.jetbrains.compose.resources.stringResource

/** Cap rendered rows; this section sits inside the editor's LazyColumn (not itself lazy). */
private const val MEMBER_RENDER_CAP = 100

/**
 * Collapsible "Members" section for the squad editors. Members load lazily on first expand,
 * so opening a squad to edit config doesn't trigger the all-users fetch.
 */
@Composable
internal fun SquadMembersCard(
    expanded: Boolean,
    loading: Boolean,
    members: List<SquadMember>,
    count: Int,
    error: String?,
    onToggle: () -> Unit,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FwSectionIcon(Icons.Rounded.People, MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(Res.string.squads_edit_members),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        "$count",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (expanded) {
                when {
                    loading -> Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    }

                    error != null -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = onRetry) { Text(stringResource(Res.string.common_retry)) }
                    }

                    members.isEmpty() -> Text(
                        stringResource(Res.string.squads_members_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    else -> {
                        members.take(MEMBER_RENDER_CAP).forEach { MemberRow(it) }
                        if (members.size > MEMBER_RENDER_CAP) {
                            Text(
                                stringResource(Res.string.squads_members_showing, MEMBER_RENDER_CAP, members.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(member: SquadMember) {
    val fwStatus = LocalFwStatus.current
    val color = when (member.status) {
        UserStatus.ACTIVE   -> fwStatus.online
        UserStatus.LIMITED  -> fwStatus.warning
        UserStatus.EXPIRED  -> fwStatus.offline
        UserStatus.DISABLED -> fwStatus.neutral
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(
            member.username,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Text(
            member.status.localized().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
