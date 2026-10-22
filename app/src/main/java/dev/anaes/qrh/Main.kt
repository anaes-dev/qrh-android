package dev.anaes.qrh

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
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
import dev.anaes.qrh.ui.about.AboutScreen
import dev.anaes.qrh.ui.components.BreadcrumbEntry
import dev.anaes.qrh.ui.detail.GuidelineDetailScreen
import dev.anaes.qrh.ui.disclaimers.DisclaimersScreen
import dev.anaes.qrh.ui.firstrun.FirstRunScreen
import dev.anaes.qrh.ui.list.GuidelineListScreen
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

@Serializable
data class DetailRoute(val code: String)

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
        factory = viewModelFactory { initializer { QrhViewModel(repository) } }
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

    NavHost(
        navController = navController,
        startDestination = startDest,
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
            GuidelineListScreen(
                viewModel = viewModel,
                onGuidelineClick = { guideline ->
                    navController.navigate(DetailRoute(guideline.code))
                },
                onAboutClick = {
                    navController.navigate(AboutRoute)
                },
            )
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
                copyright = viewModel.getCopyright(guideline),
                breadcrumbs = breadcrumbs,
                preferences = preferences,
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
