package art.yniyniyni.freedomwave.ui.feature.welcome

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.ui.feature.login.LoginContent
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.welcome_slide1_body
import freedomwave.composeapp.generated.resources.welcome_slide1_title
import freedomwave.composeapp.generated.resources.welcome_slide2_body
import freedomwave.composeapp.generated.resources.welcome_slide2_title
import freedomwave.composeapp.generated.resources.welcome_slide3_body
import freedomwave.composeapp.generated.resources.welcome_slide3_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val PAGE_COUNT = 3

/**
 * First-launch / logged-out flow: a one-time wave splash, then a 3-page carousel ending on the
 * real login form. Shown by [App] whenever the user is not logged in.
 */
@Composable
fun WelcomeScreen() {
    // rememberSaveable so the splash plays once per appearance (survives rotation, not cold start).
    var splashDone by rememberSaveable { mutableStateOf(false) }

    Crossfade(targetState = splashDone, animationSpec = tween(200), label = "welcome") { done ->
        if (!done) {
            WaveSplash(onFinished = { splashDone = true })
        } else {
            WelcomePager()
        }
    }
}

@Composable
private fun WelcomePager() {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize().imePadding(),
    ) { page ->
        when (page) {
            0 -> OnboardingPage(
                pageIndex = 0,
                pageCount = PAGE_COUNT,
                title = stringResource(Res.string.welcome_slide1_title),
                body = stringResource(Res.string.welcome_slide1_body),
                onNext = { scope.launch { pagerState.animateScrollToPage(WelcomeNav.nextPage(0, PAGE_COUNT)) } },
                onSkip = { scope.launch { pagerState.animateScrollToPage(WelcomeNav.skipTarget(PAGE_COUNT)) } },
                hero = { DashboardGlanceHero() },
            )
            1 -> OnboardingPage(
                pageIndex = 1,
                pageCount = PAGE_COUNT,
                title = stringResource(Res.string.welcome_slide2_title),
                body = stringResource(Res.string.welcome_slide2_body),
                onNext = { scope.launch { pagerState.animateScrollToPage(WelcomeNav.nextPage(1, PAGE_COUNT)) } },
                onSkip = { scope.launch { pagerState.animateScrollToPage(WelcomeNav.skipTarget(PAGE_COUNT)) } },
                hero = { FleetGlanceHero() },
            )
            else -> LoginPage()
        }
    }
}

/** The login page (carousel page 2): dots + header, then the real login form. */
@Composable
private fun LoginPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        WelcomeDots(pageCount = PAGE_COUNT, currentPage = 2)
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.welcome_slide3_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.welcome_slide3_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        LoginContent(modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
    }
}
