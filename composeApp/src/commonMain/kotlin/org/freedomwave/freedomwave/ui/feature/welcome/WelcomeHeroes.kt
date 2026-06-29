package org.freedomwave.ui.feature.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.freedomwave.ui.theme.LocalFwMonoFont

private val StatusGreen = Color(0xFF46D17F)

@Composable
private fun MiniCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) { content() }
}

@Composable
private fun StatRow(label: String, value: String, valueAccent: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontFamily = LocalFwMonoFont.current,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (valueAccent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Slide 1 hero: a compact "your panel at a glance" snapshot. */
@Composable
fun DashboardGlanceHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        MiniCard { StatRow("Panel", "v2.1.4") }
        MiniCard { StatRow("Uptime", "12d 4h") }
        MiniCard { StatRow("Online now", "128", valueAccent = true) }
    }
}

/** Slide 2 hero: stat card + a user row (ACTIVE) + a node row (status dot + country flag). */
@Composable
fun FleetGlanceHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        MiniCard { StatRow("Online now", "128 / 1.2k", valueAccent = true) }
        MiniCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(
                        Modifier.size(26.dp).clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Rounded.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)) }
                    Column(Modifier.weight(1f)) {
                        Text("alex_node", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Text("4.2 GB / 50 GB", fontFamily = LocalFwMonoFont.current, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusPill("ACTIVE")
                }
            }
            MiniCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Box(Modifier.size(9.dp).clip(RoundedCornerShape(50)).background(StatusGreen))
                    Text("de-fra-01", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text("DE 🇩🇪", fontFamily = LocalFwMonoFont.current, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

@Composable
private fun StatusPill(text: String) {
    Box(
        Modifier.clip(RoundedCornerShape(50))
            .background(StatusGreen.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text, color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp, style = MaterialTheme.typography.labelSmall)
    }
}
