package dev.anaes.qrh

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// --- Type-safe route definitions ---

@Serializable
data class FirstRunRoute(val isUpdate: Boolean)

@Serializable
object ListRoute

/**
 * A guideline. Opened from a search match inside it, [focusCard] is the content item to scroll to
 * and [query] the search whose words are highlighted.
 */
@Serializable
data class DetailRoute(val code: String, val focusCard: Int = -1, val query: String = "")

@Serializable
data class SwipeRoute(val code: String)

@Serializable
object AboutRoute

@Serializable
object DisclaimersRoute

// --- Animation constants ---

/** Duration for slide/combined navigation transitions (ms). */
private const val NAV_DURATION = 300

/** Duration for simple fade transitions (ms). */
private const val FADE_DURATION = 200

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
                    window.setBackgroundDrawable(ColorDrawable(bgColor))
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
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Disclaimer status as it was at launch. Read once and kept across recreation, so accepting
    // the disclaimers (which changes the stored status) doesn't swap the NavHost's start
    // destination underneath the back stack.
    var launchStatus by rememberSaveable { mutableStateOf<DisclaimerStatus?>(null) }
    LaunchedEffect(Unit) {
        if (launchStatus == null) launchStatus = preferences.disclaimerStatus.first()
    }

    val viewModel: QrhViewModel = viewModel(
        factory = viewModelFactory { initializer { QrhViewModel(repository, createSavedStateHandle()) } }
    )

    // Every screen except the first-run disclaimers has a red / dark grey top bar, so needs
    // light status bar icons; first run (and the loading screen) shows the plain background.
    val currentEntry by navController.currentBackStackEntryAsState()
    val onFirstRun = launchStatus == null ||
        currentEntry?.destination?.hasRoute<FirstRunRoute>() == true
    val lightStatusBarIcons = darkTheme || !onFirstRun
    val activity = LocalActivity.current as? ComponentActivity
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

    // Wait for DataStore to load before deciding start destination
    val status = launchStatus
    if (status == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDest: Any = when (status) {
        DisclaimerStatus.ACCEPTED -> ListRoute
        DisclaimerStatus.FIRST_RUN -> FirstRunRoute(isUpdate = false)
        DisclaimerStatus.UPDATED -> FirstRunRoute(isUpdate = true)
    }

    // Tablets show the guideline list beside the open guideline (as the iPad sidebar does), and
    // guidelines in two columns. The back stack is the same either way, so folding, unfolding or
    // resizing the window keeps the user's place.
    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= TABLET_MIN_SMALLEST_WIDTH_DP
    // The open guidelines, bottom to top
    val openRoutes = remember(currentEntry) {
        navController.currentBackStack.value
            .filter { navEntry -> navEntry.destination.hasRoute<DetailRoute>() }
            .map { navEntry -> navEntry.toRoute<DetailRoute>() }
    }
    // Shared by the list pane and the list screen, and kept while the pane is hidden
    val guidelineListState = rememberLazyListState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val twoPane = isTablet && maxWidth >= TWO_PANE_MIN_WIDTH
        val guidelineWidth = if (twoPane) maxWidth - LIST_PANE_WIDTH else maxWidth
        // Read by the destinations through State, so the NavHost's graph isn't rebuilt when they change
        val twoPaneState = rememberUpdatedState(twoPane)
        val twoColumnsState = rememberUpdatedState(isTablet && guidelineWidth >= TWO_COLUMN_MIN_WIDTH)
        val destination = currentEntry?.destination
        val showListPane = twoPane &&
            (destination?.hasRoute<ListRoute>() == true || destination?.hasRoute<DetailRoute>() == true)

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
                        onGuidelineClick = { guideline, result ->
                            val route = detailRoute(guideline, result, viewModel.searchQuery)
                            // Replaces the open guidelines, unless it's already open, on its own, the same way
                            if (openRoutes != listOf(route)) {
                                navController.navigate(route) { popUpTo<ListRoute>() }
                            }
                        },
                        onAboutClick = {
                            navController.navigate(AboutRoute)
                        },
                        selectedCode = openRoutes.firstOrNull()?.code,
                        listState = guidelineListState,
                        modifier = Modifier.weight(1f),
                    )
                    VerticalDivider()
                }
            }

            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (showListPane) {
                            Modifier.consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                        } else {
                            Modifier
                        }
                    ),
                enterTransition = { fadeIn(tween(FADE_DURATION)) },
                exitTransition = { fadeOut(tween(FADE_DURATION)) },
                popEnterTransition = { fadeIn(tween(FADE_DURATION)) },
                popExitTransition = { fadeOut(tween(FADE_DURATION)) },
            ) {

                // First run — fade through
                composable<FirstRunRoute>(
                    enterTransition = { fadeIn(tween(FADE_DURATION)) },
                    exitTransition = { fadeOut(tween(FADE_DURATION)) },
                ) { entry ->
                    val route = entry.toRoute<FirstRunRoute>()
                    FirstRunScreen(
                        isUpdate = route.isUpdate,
                        onExit = { activity?.finish() },
                        onAgree = {
                            scope.launch {
                                preferences.acceptDisclaimers()
                                navController.navigate(ListRoute) {
                                    popUpTo<FirstRunRoute> { inclusive = true }
                                }
                            }
                        },
                    )
                }

                // List — slides out left + scales down when pushing detail, reverses when popping
                composable<ListRoute>(
                    enterTransition = { fadeIn(tween(FADE_DURATION)) },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 4 },
                            animationSpec = tween(NAV_DURATION, easing = EaseIn)
                        ) + fadeOut(tween(NAV_DURATION)) + scaleOut(
                            targetScale = 0.94f,
                            animationSpec = tween(NAV_DURATION, easing = EaseIn)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / 4 },
                            animationSpec = tween(NAV_DURATION, easing = EaseOut)
                        ) + fadeIn(tween(NAV_DURATION)) + scaleIn(
                            initialScale = 0.94f,
                            animationSpec = tween(NAV_DURATION, easing = EaseOut)
                        )
                    },
                    popExitTransition = { fadeOut(tween(FADE_DURATION)) },
                ) {
                    if (twoPaneState.value) {
                        // The list is in the pane beside this
                        NoGuidelineSelectedScreen()
                    } else {
                        GuidelineListScreen(
                            viewModel = viewModel,
                            onGuidelineClick = { guideline, result ->
                                navController.navigate(detailRoute(guideline, result, viewModel.searchQuery))
                            },
                            onAboutClick = {
                                navController.navigate(AboutRoute)
                            },
                            listState = guidelineListState,
                        )
                    }
                }

                // Detail — slides in from right; slides out left + scales when pushing another detail/swipe
                composable<DetailRoute>(
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it / 3 },
                            animationSpec = tween(NAV_DURATION, easing = EaseOut)
                        ) + fadeIn(tween(NAV_DURATION))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 4 },
                            animationSpec = tween(NAV_DURATION, easing = EaseIn)
                        ) + fadeOut(tween(NAV_DURATION)) + scaleOut(
                            targetScale = 0.94f,
                            animationSpec = tween(NAV_DURATION, easing = EaseIn)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / 4 },
                            animationSpec = tween(NAV_DURATION, easing = EaseOut)
                        ) + fadeIn(tween(NAV_DURATION)) + scaleIn(
                            initialScale = 0.94f,
                            animationSpec = tween(NAV_DURATION, easing = EaseOut)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it / 3 },
                            animationSpec = tween(NAV_DURATION, easing = EaseIn)
                        ) + fadeOut(tween(NAV_DURATION))
                    },
                ) { entry ->
                    val route = entry.toRoute<DetailRoute>()
                    val guideline = viewModel.getGuideline(route.code) ?: return@composable

                    val backStack by navController.currentBackStackEntryAsState()
                    val breadcrumbs = remember(backStack) {
                        navController.currentBackStack.value
                            .filter { navEntry -> navEntry.destination.hasRoute<DetailRoute>() }
                            .mapNotNull { navEntry ->
                                val detailRoute = navEntry.toRoute<DetailRoute>()
                                val entryGuideline = repository.getGuideline(detailRoute.code) ?: return@mapNotNull null
                                BreadcrumbEntry(detailRoute.code, entryGuideline.title)
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
                        // Beside the list, the first guideline has nothing to go back to
                        showBack = !(twoPaneState.value && breadcrumbs.size <= 1),
                        onNavigateBack = { navController.popBackStack() },
                        onHomeClick = {
                            navController.popBackStack<ListRoute>(inclusive = false)
                        },
                        onBreadcrumbClick = { index ->
                            val entriesToPop = breadcrumbs.size - 1 - index
                            repeat(entriesToPop) {
                                navController.popBackStack()
                            }
                        },
                        onGuidelineLink = { linkedCode ->
                            if (linkedCode != route.code) {
                                navController.navigate(DetailRoute(linkedCode))
                            }
                        },
                        onSwipeView = {
                            navController.navigate(SwipeRoute(route.code))
                        },
                    )
                }

                // Swipe view — slides up from bottom (mode change), slides back down when popping
                composable<SwipeRoute>(
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(NAV_DURATION, easing = EaseOut)
                        ) + fadeIn(tween(NAV_DURATION))
                    },
                    exitTransition = { fadeOut(tween(FADE_DURATION)) },
                    popEnterTransition = { fadeIn(tween(FADE_DURATION)) },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(NAV_DURATION, easing = EaseIn)
                        ) + fadeOut(tween(NAV_DURATION))
                    },
                ) { entry ->
                    val route = entry.toRoute<SwipeRoute>()
                    val guideline = viewModel.getGuideline(route.code) ?: return@composable

                    SwipeViewScreen(
                        guideline = guideline,
                        onNavigateBack = { navController.popBackStack() },
                        onGuidelineLink = { linkedCode ->
                            navController.popBackStack()
                            navController.navigate(DetailRoute(linkedCode))
                        },
                    )
                }

                // About — slide up from bottom (overlay feel); slides left when pushing disclaimers
                composable<AboutRoute>(
                    enterTransition = {
                        slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(NAV_DURATION, easing = EaseOut)
                        ) + fadeIn(tween(FADE_DURATION))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { -it / 4 },
                            animationSpec = tween(NAV_DURATION, easing = EaseIn)
                        ) + fadeOut(tween(NAV_DURATION)) + scaleOut(
                            targetScale = 0.94f,
                            animationSpec = tween(NAV_DURATION, easing = EaseIn)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { -it / 4 },
                            animationSpec = tween(NAV_DURATION, easing = EaseOut)
                        ) + fadeIn(tween(NAV_DURATION)) + scaleIn(
                            initialScale = 0.94f,
                            animationSpec = tween(NAV_DURATION, easing = EaseOut)
                        )
                    },
                    popExitTransition = {
                        slideOutVertically(
                            targetOffsetY = { it / 4 },
                            animationSpec = tween(NAV_DURATION, easing = EaseIn)
                        ) + fadeOut(tween(FADE_DURATION))
                    },
                ) {
                    AboutScreen(
                        preferences = preferences,
                        onNavigateBack = { navController.popBackStack() },
                        onViewDisclaimers = {
                            navController.navigate(DisclaimersRoute)
                        },
                    )
                }

                // Disclaimers — slide in from right (sub-page of about)
                composable<DisclaimersRoute>(
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { it / 3 },
                            animationSpec = tween(NAV_DURATION, easing = EaseOut)
                        ) + fadeIn(tween(NAV_DURATION))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { it / 3 },
                            animationSpec = tween(NAV_DURATION, easing = EaseIn)
                        ) + fadeOut(tween(NAV_DURATION))
                    },
                ) {
                    DisclaimersScreen(
                        onNavigateBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

/** The route for opening [guideline] from the list: at the matching item if found by its content. */
private fun detailRoute(guideline: Guideline, result: SearchResult?, query: String): DetailRoute =
    if (result != null && result.isContentMatch) {
        DetailRoute(guideline.code, focusCard = result.cardIndex, query = query)
    } else {
        DetailRoute(guideline.code)
    }
