package dev.anaes.qrh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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

@OptIn(ExperimentalSharedTransitionApi::class)
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

    SharedTransitionLayout {
        NavHost(navController = navController, startDestination = startDest) {

            composable(
                route = "firstrun/{isUpdate}",
                arguments = listOf(navArgument("isUpdate") { type = NavType.BoolType })
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

            composable("list") {
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

            composable(
                route = "detail/{code}",
                arguments = listOf(navArgument("code") { type = NavType.StringType })
            ) { entry ->
                val code = entry.arguments?.getString("code") ?: return@composable
                val guideline = viewModel.getGuideline(code) ?: return@composable

                // Build breadcrumbs from back stack
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
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
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

            composable(
                route = "swipe/{code}",
                arguments = listOf(navArgument("code") { type = NavType.StringType })
            ) { entry ->
                val code = entry.arguments?.getString("code") ?: return@composable
                val guideline = viewModel.getGuideline(code) ?: return@composable

                SwipeViewScreen(
                    guideline = guideline,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    onNavigateBack = { navController.popBackStack() },
                    onGuidelineLink = { linkedCode ->
                        navController.popBackStack()
                        navController.navigate("detail/$linkedCode")
                    },
                )
            }

            composable("about") {
                AboutScreen(
                    preferences = preferences,
                    onNavigateBack = { navController.popBackStack() },
                    onViewDisclaimers = {
                        navController.navigate("disclaimers")
                    },
                )
            }

            composable("disclaimers") {
                DisclaimersScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
