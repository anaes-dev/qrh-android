package dev.anaes.qrh

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import dev.anaes.qrh.data.DisclaimerStatus
import dev.anaes.qrh.data.GuidelineRepository
import dev.anaes.qrh.data.UserPreferences
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.search.SearchResult
import dev.anaes.qrh.ui.about.AboutScreen
import dev.anaes.qrh.ui.components.BreadcrumbEntry
import dev.anaes.qrh.ui.detail.GuidelineDetailScreen
import dev.anaes.qrh.ui.disclaimers.DisclaimersScreen
import dev.anaes.qrh.ui.firstrun.FirstRunScreen
import dev.anaes.qrh.ui.list.GuidelineListScreen
import dev.anaes.qrh.ui.list.NoGuidelineSelectedScreen
import dev.anaes.qrh.ui.swipe.SwipeViewScreen
import dev.anaes.qrh.ui.theme.QrhTheme
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

// --- Type-safe route definitions ---

/** A screen. Sealed, so the back stack is saved with generated serializers (no reflection). */
@Serializable
sealed interface Route : NavKey

@Serializable
data class FirstRunRoute(val isUpdate: Boolean) : Route

@Serializable
data object ListRoute : Route

/**
 * A guideline. Opened from a search match inside it, [focusCard] is the content item to scroll to
 * and [query] the search whose words are highlighted. [id] tells apart two visits to the same
 * guideline on the back stack (3-1 → 3-2 → 3-1), so each keeps its own scroll position and boxes.
 */
@Serializable
data class DetailRoute(
    val code: String,
    val focusCard: Int = -1,
    val query: String = "",
    val id: String = UUID.randomUUID().toString(),
) : Route {
    /** Whether this opens the same guideline, at the same place, as [other]. */
    fun opensSameAs(other: DetailRoute): Boolean = copy(id = other.id) == other
}

@Serializable
data class SwipeRoute(val code: String) : Route

@Serializable
data object AboutRoute : Route

@Serializable
data object DisclaimersRoute : Route

/** The metadata key that holds each entry's route, so the transitions can tell screens apart. */
private const val ROUTE_METADATA = "route"

// --- Animation constants ---

/** Guideline slides (ms), as the original's: the platform's config_mediumAnimTime. */
private const val SLIDE_DURATION = 400

/** Cross-fades (ms), as Navigation's default animations: navigation-ui's config_navAnimTime. */
private const val FADE_DURATION = 150

/** The scale guidelines slide in from, and the screen below them out to. */
private const val SLIDE_SCALE = 0.8f

/** The original's scale animations set no pivot, so scaled about the top left corner. */
private val SLIDE_SCALE_ORIGIN = TransformOrigin(0f, 0f)

/** The View animations' default, AccelerateDecelerateInterpolator. */
private val AccelerateDecelerateEasing = Easing(AccelerateDecelerateInterpolator()::getInterpolation)

// --- Tablet layout (as on iPad) ---

/** Smallest screen width of a tablet (or unfolded foldable); phones in landscape stay single-pane. */
private const val TABLET_MIN_SMALLEST_WIDTH_DP = 600

/** Window width from which the guideline list stays beside the open guideline. */
private val TWO_PANE_MIN_WIDTH = 840.dp

private val LIST_PANE_WIDTH = 360.dp

/** Width from which a guideline shows its boxes and images in a column beside its steps. */
private val TWO_COLUMN_MIN_WIDTH = 600.dp

// Navigation bar scrims, as used by androidx.activity's default edge-to-edge styling
private val NAV_BAR_LIGHT_SCRIM = AndroidColor.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val NAV_BAR_DARK_SCRIM = AndroidColor.argb(0x80, 0x1b, 0x1b, 0x1b)

class Main : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as QrhApplication
        val repository = app.repository
        val preferences = app.preferences

        setContent {
            val nightDisabled by preferences.nightDisabled.collectAsState(initial = false)
            val darkTheme = if (nightDisabled) false else isSystemInDarkTheme()

            QrhTheme(darkTheme = darkTheme) {
                // Sync the Activity window background with the Compose theme so
                // navigation transitions don't reveal a mismatched background
                // (e.g. dark window behind light Compose content).
                val bgColor = MaterialTheme.colorScheme.background.toArgb()
                SideEffect {
                    window.setBackgroundDrawable(bgColor.toDrawable())
                }

                QrhApp(repository = repository, preferences = preferences, darkTheme = darkTheme)
            }
        }
    }
}

@Composable
fun QrhApp(
    repository: GuidelineRepository,
    preferences: UserPreferences,
    darkTheme: Boolean,
) {
    val scope = rememberCoroutineScope()

    // Disclaimer status as it was at launch. Read once and kept across recreation, so accepting
    // the disclaimers (which changes the stored status) doesn't swap the start route underneath
    // the back stack.
    var launchStatus by rememberSaveable { mutableStateOf<DisclaimerStatus?>(null) }
    LaunchedEffect(Unit) {
        if (launchStatus == null) launchStatus = preferences.disclaimerStatus.first()
    }

    val viewModel: QrhViewModel = viewModel(
        factory = viewModelFactory { initializer { QrhViewModel(repository, createSavedStateHandle()) } }
    )
    val activity = LocalActivity.current as? ComponentActivity

    // Wait for DataStore to load before deciding the start route
    val status = launchStatus
    if (status == null) {
        SystemBars(activity, darkTheme, lightStatusBarIcons = darkTheme)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val backStack = rememberRouteBackStack(
        when (status) {
            DisclaimerStatus.ACCEPTED -> ListRoute
            DisclaimerStatus.FIRST_RUN -> FirstRunRoute(isUpdate = false)
            DisclaimerStatus.UPDATED -> FirstRunRoute(isUpdate = true)
        },
    )
    val currentRoute = backStack.last()

    // Every screen except the first-run disclaimers has a red / dark grey top bar, so needs
    // light status bar icons; first run (and the loading screen) shows the plain background.
    SystemBars(activity, darkTheme, lightStatusBarIcons = darkTheme || currentRoute !is FirstRunRoute)

    // Tablets show the guideline list beside the open guideline (as the iPad sidebar does), and
    // guidelines in two columns. The back stack is the same either way, so folding, unfolding or
    // resizing the window keeps the user's place.
    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP
    // The open guidelines, bottom to top
    val openRoutes = backStack.filterIsInstance<DetailRoute>()
    // Shared by the list pane and the list screen, and kept while the pane is hidden
    val guidelineListState = rememberLazyListState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val twoPane = isTablet && maxWidth >= TWO_PANE_MIN_WIDTH
        val guidelineWidth = if (twoPane) maxWidth - LIST_PANE_WIDTH else maxWidth
        val twoColumns = isTablet && guidelineWidth >= TWO_COLUMN_MIN_WIDTH
        val showListPane = twoPane && backStack.isPaneActive()
        // Read by the screens through State: NavDisplay only rebuilds an entry's content when the
        // back stack changes, so a resized window (split screen, folding) would leave copies stale
        val twoPaneState = rememberUpdatedState(twoPane)
        val twoColumnsState = rememberUpdatedState(twoColumns)

        Row(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = showListPane) {
                // Each pane keeps clear of a display cutout only on the screen edge it's at
                Row(
                    modifier = Modifier
                        .width(LIST_PANE_WIDTH)
                        .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.End)),
                ) {
                    GuidelineListScreen(
                        viewModel = viewModel,
                        // Both read the back stack itself, which a first tap has already changed, so a
                        // quick second tap (or one while the pane hides for About) does nothing more
                        onGuidelineClick = { guideline, result ->
                            val route = detailRoute(guideline, result, viewModel.searchQuery)
                            val open = backStack.filterIsInstance<DetailRoute>()
                            // Replaces the open guidelines, unless it's already open, on its own, the same way
                            if (backStack.isPaneActive() && (open.size != 1 || !open.single().opensSameAs(route))) {
                                backStack.popAbove(ListRoute)
                                backStack.add(route)
                            }
                        },
                        onAboutClick = {
                            if (backStack.isPaneActive()) backStack.add(AboutRoute)
                        },
                        // The guideline showing, which may be one opened from a link in another
                        selectedCode = openRoutes.lastOrNull()?.code,
                        // Back closes the open guidelines before it clears the search
                        clearSearchOnBack = openRoutes.isEmpty(),
                        listState = guidelineListState,
                        modifier = Modifier.weight(1f),
                    )
                    VerticalDivider()
                }
            }

            NavDisplay(
                backStack = backStack,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (showListPane) {
                            Modifier.consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                        } else {
                            Modifier
                        }
                    )
                    // The slides would otherwise pass over the list pane
                    .clipToBounds(),
                onBack = { backStack.pop() },
                transitionSpec = {
                    if (targetState.route is DetailRoute) slideTransition(forward = true) else fadeTransition()
                },
                popTransitionSpec = {
                    if (initialState.route is DetailRoute) slideTransition(forward = false) else fadeTransition()
                },
                predictivePopTransitionSpec = {
                    if (initialState.route is DetailRoute) slideTransition(forward = false) else fadeTransition()
                },
                entryProvider = { route ->
                    NavEntry(route, metadata = mapOf(ROUTE_METADATA to route)) {
                        when (route) {
                            is FirstRunRoute -> FirstRunScreen(
                                isUpdate = route.isUpdate,
                                onExit = { activity?.finish() },
                                onAgree = {
                                    scope.launch {
                                        preferences.acceptDisclaimers()
                                        backStack.navigateFrom(route) {
                                            add(ListRoute)
                                            removeAll { it is FirstRunRoute }
                                        }
                                    }
                                },
                            )

                            ListRoute -> if (twoPaneState.value) {
                                // The list is in the pane beside this
                                NoGuidelineSelectedScreen()
                            } else {
                                GuidelineListScreen(
                                    viewModel = viewModel,
                                    onGuidelineClick = { guideline, result ->
                                        backStack.navigateFrom(route) { add(detailRoute(guideline, result, viewModel.searchQuery)) }
                                    },
                                    onAboutClick = {
                                        backStack.navigateFrom(route) { add(AboutRoute) }
                                    },
                                    listState = guidelineListState,
                                )
                            }

                            is DetailRoute -> {
                                val guideline = viewModel.getGuideline(route.code) ?: return@NavEntry

                                // The guidelines opened on the way here, ending with this one. Fixed while
                                // it's open, as only the screens above it can change.
                                val trail = remember(route) {
                                    val index = backStack.indexOf(route)
                                    if (index >= 0) backStack.subList(0, index + 1).filterIsInstance<DetailRoute>() else listOf(route)
                                }
                                val breadcrumbs = remember(trail) {
                                    trail.mapNotNull { trailRoute ->
                                        val trailGuideline = repository.getGuideline(trailRoute.code) ?: return@mapNotNull null
                                        BreadcrumbEntry(trailRoute.code, trailGuideline.title)
                                    }
                                }

                                GuidelineDetailScreen(
                                    guideline = guideline,
                                    focusCard = route.focusCard,
                                    highlightQuery = route.query,
                                    copyright = viewModel.getCopyright(guideline),
                                    breadcrumbs = breadcrumbs,
                                    preferences = preferences,
                                    twoColumns = twoColumnsState.value,
                                    // Beside the list, the bar stays a single row
                                    compactTopBar = twoPaneState.value,
                                    // Beside the list, the first guideline has nothing to go back to
                                    showBack = !(twoPaneState.value && breadcrumbs.size <= 1),
                                    onNavigateBack = { backStack.navigateFrom(route) { pop() } },
                                    onHomeClick = { backStack.navigateFrom(route) { popAbove(ListRoute) } },
                                    onBreadcrumbClick = { index -> backStack.navigateFrom(route) { popAbove(trail[index]) } },
                                    onGuidelineLink = { linkedCode ->
                                        if (linkedCode != route.code) {
                                            backStack.navigateFrom(route) { add(DetailRoute(linkedCode)) }
                                        }
                                    },
                                    onSwipeView = {
                                        backStack.navigateFrom(route) { add(SwipeRoute(route.code)) }
                                    },
                                )
                            }

                            is SwipeRoute -> {
                                val guideline = viewModel.getGuideline(route.code) ?: return@NavEntry

                                SwipeViewScreen(
                                    guideline = guideline,
                                    onNavigateBack = { backStack.navigateFrom(route) { pop() } },
                                    onGuidelineLink = { linkedCode ->
                                        backStack.navigateFrom(route) {
                                            pop()
                                            add(DetailRoute(linkedCode))
                                        }
                                    },
                                )
                            }

                            AboutRoute -> AboutScreen(
                                preferences = preferences,
                                onNavigateBack = { backStack.navigateFrom(route) { pop() } },
                                onViewDisclaimers = {
                                    backStack.navigateFrom(route) { add(DisclaimersRoute) }
                                },
                            )

                            DisclaimersRoute -> DisclaimersScreen(
                                onNavigateBack = { backStack.navigateFrom(route) { pop() } },
                            )
                        }
                    }
                },
            )
        }
    }
}

/** The status and navigation bar styles, matching the current screen's top bar and theme. */
@Composable
private fun SystemBars(activity: ComponentActivity?, darkTheme: Boolean, lightStatusBarIcons: Boolean) {
    DisposableEffect(activity, darkTheme, lightStatusBarIcons) {
        activity?.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT,
            ) { lightStatusBarIcons },
            navigationBarStyle = SystemBarStyle.auto(
                NAV_BAR_LIGHT_SCRIM,
                NAV_BAR_DARK_SCRIM,
            ) { darkTheme },
        )
        onDispose {}
    }
}

/** The back stack, starting at [start], saved across configuration changes and process death. */
@Composable
private fun rememberRouteBackStack(start: Route): NavBackStack<Route> =
    rememberSerializable(serializer = serializer()) { NavBackStack(start) }

/**
 * Runs [action] only while [from] is the screen on top. A screen's buttons stay tappable while it
 * animates away, so this stops a quick second tap navigating again; the back stack is updated at
 * once, so there's no window for it to slip through. (`dropUnlessResumed` would leave one: Navigation
 * 3 only moves the screen out of RESUMED from an effect, after it next recomposes.)
 */
private inline fun NavBackStack<Route>.navigateFrom(from: Route, action: NavBackStack<Route>.() -> Unit) {
    if (lastOrNull() == from) action()
}

/** Whether the tablet list pane is showing, beside the list's placeholder or the open guidelines. */
private fun NavBackStack<Route>.isPaneActive(): Boolean = last().let { it is ListRoute || it is DetailRoute }

/** Goes back a screen, unless this is the first. */
private fun NavBackStack<Route>.pop() {
    if (size > 1) removeAt(lastIndex)
}

/** Goes back to [route], in one step. */
private fun NavBackStack<Route>.popAbove(route: Route) {
    val index = indexOf(route)
    if (index < 0) return
    while (lastIndex > index) removeAt(lastIndex)
}

// --- Transitions ---
// As in the original app's navigation graph: opening a guideline slides it in from the right as
// the screen below slides out to the left, and every other screen cross-fades. As there, the
// screen being opened (or closed) picks the transition for both.

/** The route a scene shows, from its entry's metadata. */
private val Scene<Route>.route: Route
    get() = entries.last().metadata[ROUTE_METADATA] as Route

/** The two screens slide side by side, a whole width, fading and growing from [SLIDE_SCALE]. */
private fun slideTransition(forward: Boolean): ContentTransform {
    val direction = if (forward) 1 else -1
    return slideInHorizontally(slideSpec()) { direction * it } +
        fadeIn(slideSpec()) +
        scaleIn(slideSpec(), SLIDE_SCALE, SLIDE_SCALE_ORIGIN) togetherWith
        slideOutHorizontally(slideSpec()) { -direction * it } +
        fadeOut(slideSpec()) +
        scaleOut(slideSpec(), SLIDE_SCALE, SLIDE_SCALE_ORIGIN)
}

private fun <T> slideSpec() = tween<T>(SLIDE_DURATION, easing = AccelerateDecelerateEasing)

/** Navigation's default transition. */
private fun fadeTransition(): ContentTransform =
    fadeIn(tween(FADE_DURATION, easing = LinearEasing)) togetherWith
        fadeOut(tween(FADE_DURATION, easing = LinearEasing))

/** The route for opening [guideline] from the list: at the matching item if found by its content. */
private fun detailRoute(guideline: Guideline, result: SearchResult?, query: String): DetailRoute =
    if (result != null && result.isContentMatch) {
        DetailRoute(guideline.code, focusCard = result.cardIndex, query = query)
    } else {
        DetailRoute(guideline.code)
    }
