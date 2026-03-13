package dev.anaes.qrh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import kotlinx.coroutines.launch

private const val NAV_DURATION = 300
private const val FADE_DURATION = 200

class Main : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = GuidelineRepository(assets)
        val preferences = UserPreferences(applicationContext)

        setContent {
            val nightDisabled by preferences.nightDisabled.collectAsState(initial = false)
            val darkTheme = if (nightDisabled) false else isSystemInDarkTheme()

            QrhTheme(darkTheme = darkTheme) {
                QrhApp(repository = repository, preferences = preferences)
            }
        }
    }
}

@Composable
fun QrhApp(
    repository: GuidelineRepository,
    preferences: UserPreferences,
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val disclaimersAccepted by preferences.disclaimersAccepted.collectAsState(initial = null)

    val viewModel: QrhViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QrhViewModel(repository) as T
            }
        }
    )

    // Wait for DataStore to load before deciding start destination
    val startDest = when (disclaimersAccepted) {
        null -> return // Still loading
        true -> "list"
        false -> "firstrun/false"
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
        composable(
            route = "firstrun/{isUpdate}",
            arguments = listOf(navArgument("isUpdate") { type = NavType.BoolType }),
            enterTransition = { fadeIn(tween(FADE_DURATION)) },
            exitTransition = { fadeOut(tween(FADE_DURATION)) },
        ) { entry ->
            val isUpdate = entry.arguments?.getBoolean("isUpdate") ?: false
            FirstRunScreen(
                isUpdate = isUpdate,
                onAgree = {
                    scope.launch {
                        preferences.acceptDisclaimers()
                        navController.navigate("list") {
                            popUpTo("firstrun/{isUpdate}") { inclusive = true }
                        }
                    }
                },
            )
        }

        // List — slides out left when pushing detail, slides back in from left when popping
        composable(
            route = "list",
            enterTransition = { fadeIn(tween(FADE_DURATION)) },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(NAV_DURATION, easing = EaseIn)
                ) + fadeOut(tween(NAV_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(NAV_DURATION, easing = EaseOut)
                ) + fadeIn(tween(NAV_DURATION))
            },
            popExitTransition = { fadeOut(tween(FADE_DURATION)) },
        ) {
            GuidelineListScreen(
                viewModel = viewModel,
                onGuidelineClick = { guideline ->
                    navController.navigate("detail/${guideline.code}")
                },
                onAboutClick = {
                    navController.navigate("about")
                },
            )
        }

        // Detail — slides in from right, slides out to right when popping
        composable(
            route = "detail/{code}",
            arguments = listOf(navArgument("code") { type = NavType.StringType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / 3 },
                    animationSpec = tween(NAV_DURATION, easing = EaseOut)
                ) + fadeIn(tween(NAV_DURATION))
            },
            exitTransition = {
                // When pushing swipe view: slight scale down + fade
                fadeOut(tween(NAV_DURATION))
            },
            popEnterTransition = {
                fadeIn(tween(NAV_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it / 3 },
                    animationSpec = tween(NAV_DURATION, easing = EaseIn)
                ) + fadeOut(tween(NAV_DURATION))
            },
        ) { entry ->
            val code = entry.arguments?.getString("code") ?: return@composable
            val guideline = viewModel.getGuideline(code) ?: return@composable

            val backStack by navController.currentBackStackEntryAsState()
            val breadcrumbs = remember(backStack) {
                val entries = navController.currentBackStack.value
                    .filter { it.destination.route == "detail/{code}" }
                    .mapNotNull { navEntry ->
                        val entryCode = navEntry.arguments?.getString("code") ?: return@mapNotNull null
                        val entryGuideline = repository.getGuideline(entryCode) ?: return@mapNotNull null
                        BreadcrumbEntry(entryCode, entryGuideline.title)
                    }
                entries
            }

            GuidelineDetailScreen(
                guideline = guideline,
                breadcrumbs = breadcrumbs,
                preferences = preferences,
                onNavigateBack = { navController.popBackStack() },
                onHomeClick = {
                    navController.popBackStack("list", inclusive = false)
                },
                onBreadcrumbClick = { index ->
                    val entriesToPop = breadcrumbs.size - 1 - index
                    repeat(entriesToPop) {
                        navController.popBackStack()
                    }
                },
                onGuidelineLink = { linkedCode ->
                    navController.navigate("detail/$linkedCode")
                },
                onSwipeView = {
                    navController.navigate("swipe/$code")
                },
            )
        }

        // Swipe view — slides up from bottom (mode change), slides back down when popping
        composable(
            route = "swipe/{code}",
            arguments = listOf(navArgument("code") { type = NavType.StringType }),
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
            val code = entry.arguments?.getString("code") ?: return@composable
            val guideline = viewModel.getGuideline(code) ?: return@composable

            SwipeViewScreen(
                guideline = guideline,
                onNavigateBack = { navController.popBackStack() },
                onGuidelineLink = { linkedCode ->
                    navController.popBackStack()
                    navController.navigate("detail/$linkedCode")
                },
            )
        }

        // About — slide up from bottom (overlay feel)
        composable(
            route = "about",
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(NAV_DURATION, easing = EaseOut)
                ) + fadeIn(tween(FADE_DURATION))
            },
            exitTransition = { fadeOut(tween(FADE_DURATION)) },
            popEnterTransition = { fadeIn(tween(FADE_DURATION)) },
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
                    navController.navigate("disclaimers")
                },
            )
        }

        // Disclaimers — slide in from right (sub-page of about)
        composable(
            route = "disclaimers",
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
