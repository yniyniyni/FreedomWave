@file:OptIn(ExperimentalTransitionApi::class)

package art.yniyniyni.freedomwave.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import art.yniyniyni.freedomwave.ui.l10n.UiText
import art.yniyniyni.freedomwave.ui.l10n.resolve
import art.yniyniyni.freedomwave.ui.navigation.BackGestureEffect

/** Shared interface for navigation destinations that participate in [FwNavigationContainer]. */
interface FwNavDestination {
    val depth: Int
    val key: String
}

/**
 * Shared navigation container for master-detail screens.
 *
 * Manages the navigation stack, snackbar for action errors, predictive-back transitions, and
 * AnimatedContent with a standardized slide transition. The content lambda receives the current
 * nav entry, push/pop callbacks, a lazily-evaluated current-stack accessor for async guards, and
 * the snackbar host state (so child scaffolds can host the same snackbar).
 */
@OptIn(ExperimentalTransitionApi::class)
@Composable
fun <T : FwNavDestination> FwNavigationContainer(
    navLabel: String,
    initialState: T,
    initialStack: List<T>,
    actionError: UiText?,
    onClearActionError: () -> Unit,
    contentKey: (T) -> String,
    content: @Composable (
        navEntry: T,
        push: (T) -> Unit,
        pop: () -> Unit,
        currentStack: () -> List<T>,
        snackbarHost: SnackbarHostState,
    ) -> Unit,
) {
    val snackbarHost = remember { SnackbarHostState() }

    var stack by remember { mutableStateOf(initialStack) }
    val top = stack.last()
    val canGoBack = stack.size > 1

    val actionErrorText = actionError?.resolve()
    LaunchedEffect(actionErrorText) {
        actionErrorText?.let {
            snackbarHost.showSnackbar(it)
            onClearActionError()
        }
    }

    val transitionState = remember { SeekableTransitionState<T>(initialState) }
    val transition = rememberTransition(transitionState, label = navLabel)

    LaunchedEffect(top) {
        if (transitionState.currentState != top) transitionState.animateTo(top)
    }

    BackGestureEffect(
        enabled = canGoBack,
        onProgress = { fraction -> transitionState.seekTo(fraction, stack[stack.size - 2]) },
        onCommit = {
            val target = stack[stack.size - 2]
            transitionState.animateTo(target)
            stack = stack.dropLast(1)
        },
        onCancel = { transitionState.animateTo(top) },
    )

    val push = remember { { entry: T -> stack = stack + entry } }
    val pop = remember { { stack = stack.dropLast(1) } }
    val currentStack = remember { { stack } }

    transition.AnimatedContent(
        contentKey = contentKey,
        transitionSpec = {
            val deeper = targetState.depth > initialState.depth
            if (deeper) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it / 4 }
            } else {
                slideInHorizontally { -it / 4 } togetherWith slideOutHorizontally { it }
            }.apply { targetContentZIndex = if (deeper) 1f else 0f }
        },
    ) { navEntry ->
        content(navEntry, push, pop, currentStack, snackbarHost)
    }
}
