package art.yniyniyni.freedomwave.ui.feature.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_next
import freedomwave.composeapp.generated.resources.welcome_skip
import org.jetbrains.compose.resources.stringResource

/**
 * A promo onboarding slide: optional top-right Skip, a centered hero, then dots + title + body
 * + a primary "Next" CTA. Used for pages 0 and 1 (the login page is built separately).
 */
@Composable
fun OnboardingPage(
    pageIndex: Int,
    pageCount: Int,
    title: String,
    body: String,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    hero: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp)) {
        if (WelcomeNav.isSkipVisible(pageIndex, pageCount)) {
            TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.TopEnd)) {
                Text(stringResource(Res.string.welcome_skip), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(modifier = Modifier.weight(1f, fill = false)) { hero() }
            Spacer(Modifier.height(20.dp))
            WelcomeDots(pageCount = pageCount, currentPage = pageIndex)
            Spacer(Modifier.height(20.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(percent = 50),
            ) {
                Text(stringResource(Res.string.common_next), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
