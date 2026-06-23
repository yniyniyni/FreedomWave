package art.yniyniyni.freedomwave.ui.feature.welcome

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import art.yniyniyni.freedomwave.ui.feature.login.LoginFields
import art.yniyniyni.freedomwave.ui.feature.login.LoginViewModel
import freedomwave.composeapp.generated.resources.Res
import freedomwave.composeapp.generated.resources.common_connect
import freedomwave.composeapp.generated.resources.common_next
import freedomwave.composeapp.generated.resources.welcome_skip
import freedomwave.composeapp.generated.resources.welcome_slide1_body
import freedomwave.composeapp.generated.resources.welcome_slide1_title
import freedomwave.composeapp.generated.resources.welcome_slide2_body
import freedomwave.composeapp.generated.resources.welcome_slide2_title
import freedomwave.composeapp.generated.resources.welcome_slide3_body
import freedomwave.composeapp.generated.resources.welcome_slide3_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val PAGE_COUNT = 3

/** The bottom CTA cycles through these as the carousel moves and login state changes. */
private enum class Cta { Next, Connect, Loading }

/**
 * First-launch / logged-out flow: a one-time wave splash, then a 3-page carousel ending on the
 * real login form. The dots indicator, the primary CTA and Skip are fixed chrome — only the
 * slide content (hero + text, and the login fields) moves under the swipe.
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WelcomePager() {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()
    val loginVm: LoginViewModel = koinViewModel()
    val loginState by loginVm.state.collectAsState()

    // Live carousel position (page + drag offset) so the dots indicator tracks the scroll.
    val dotPosition = { pagerState.currentPage + pagerState.currentPageOffsetFraction }
    val isLogin = pagerState.currentPage == PAGE_COUNT - 1

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Fixed brand wave behind the content — it stays put while slides swipe over it.
            WaveBackdrop(modifier = Modifier.align(Alignment.Center).fillMaxWidth().height(200.dp))
            // Only the content swipes; overscroll glow/stretch is disabled.
            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    when (page) {
                        0 -> SlideContent(
                            title = stringResource(Res.string.welcome_slide1_title),
                            body = stringResource(Res.string.welcome_slide1_body),
                            hero = { DashboardGlanceHero() },
                        )
                        1 -> SlideContent(
                            title = stringResource(Res.string.welcome_slide2_title),
                            body = stringResource(Res.string.welcome_slide2_body),
                            hero = { FleetGlanceHero() },
                        )
                        else -> LoginSlideContent(
                            title = stringResource(Res.string.welcome_slide3_title),
                            body = stringResource(Res.string.welcome_slide3_body),
                            vm = loginVm,
                        )
                    }
                }
            }
            // Fixed Skip, fading out once the login page is reached.
            val skipAlpha by animateFloatAsState(if (isLogin) 0f else 1f, label = "skip")
            TextButton(
                onClick = { scope.launch { pagerState.animateScrollToPage(WelcomeNav.skipTarget(PAGE_COUNT)) } },
                enabled = !isLogin,
                modifier = Modifier.align(Alignment.TopEnd).alpha(skipAlpha),
            ) {
                Text(stringResource(Res.string.welcome_skip), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        WelcomeBottomBar(
            position = dotPosition,
            isLogin = isLogin,
            loading = loginState.isLoading,
            onNext = {
                scope.launch { pagerState.animateScrollToPage(WelcomeNav.nextPage(pagerState.currentPage, PAGE_COUNT)) }
            },
            onConnect = loginVm::save,
        )
    }
}

/** Swipeable promo content: centered hero infographic + title + body. */
@Composable
private fun SlideContent(
    title: String,
    body: String,
    hero: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        hero()
        Spacer(Modifier.height(40.dp))
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
    }
}

/** Swipeable login content: header + the login fields (the Connect button lives in the bar). */
@Composable
private fun LoginSlideContent(
    title: String,
    body: String,
    vm: LoginViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
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
        LoginFields(vm = vm, modifier = Modifier.fillMaxWidth())
    }
}

/** Fixed bottom chrome: the scroll-driven dots and the primary CTA (Next → Connect). */
@Composable
private fun WelcomeBottomBar(
    position: () -> Float,
    isLogin: Boolean,
    loading: Boolean,
    onNext: () -> Unit,
    onConnect: () -> Unit,
) {
    val cta = when {
        !isLogin -> Cta.Next
        loading -> Cta.Loading
        else -> Cta.Connect
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WelcomeDots(pageCount = PAGE_COUNT, position = position)
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { if (isLogin) onConnect() else onNext() },
            enabled = cta != Cta.Loading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(percent = 50),
        ) {
            AnimatedContent(targetState = cta, label = "cta") { state ->
                when (state) {
                    Cta.Loading -> CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Cta.Connect -> Text(stringResource(Res.string.common_connect), fontWeight = FontWeight.Bold)
                    Cta.Next -> Text(stringResource(Res.string.common_next), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
