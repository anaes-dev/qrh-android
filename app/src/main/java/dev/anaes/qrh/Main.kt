package dev.anaes.qrh

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dev.anaes.qrh.ui.detail.DetailComposable
import dev.anaes.qrh.ui.list.ListComposable
import dev.anaes.qrh.ui.theme.QRHTheme
import dev.anaes.qrh.vm.DataViewModel
import kotlinx.coroutines.CoroutineScope

@HiltAndroidApp
class App : Application() {}

@AndroidEntryPoint
class Main : ComponentActivity() {
    private val viewModel: DataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QRHTheme {
                QRH(viewModel)
            }
        }
    }


}

@Composable
fun NavComposable(navController: NavHostController, modifier: Modifier = Modifier, viewModel: DataViewModel){
    NavHost(navController = navController, startDestination = "list", modifier = modifier) {
        composable(route = "list") {
            ListScreen(navController, viewModel)
        }
        composable(route = "guideline/{code}") {
            DetailScreen(navController, viewModel, it.arguments?.getString("code"))
        }
    }
}

@Composable
fun QRH(viewModel: DataViewModel) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    Log.d("Route", navController.currentDestination?.route.toString())


    Scaffold(
        topBar = { TopBar(scope) },
    ) { innerPadding ->
        NavComposable(navController, modifier = Modifier.padding(innerPadding), viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(scope: CoroutineScope) {
    TopAppBar(
        title = {
            Text(
                text = "QRH",
            )
        },
        actions = {
            IconButton(onClick = { /* doSomething() */ }) {
                Icon(Icons.Filled.Info, contentDescription = null)
            }
        }
    )
}

@Composable
fun ListScreen(
    navController: NavController,
    viewModel: DataViewModel
) {
    val focusManager = LocalFocusManager.current
    if(viewModel.searchString.value.isNotBlank()) {
        BackHandler(enabled = true, onBack = {
            viewModel.updateSearch("")
            focusManager.clearFocus()
        })
    }
    ListComposable(viewModel) { navCode ->
        loadDetail(navController, navCode, viewModel)
    }
}


@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: DataViewModel,
    code: String?
) {
   if(!code.isNullOrEmpty()) {
       viewModel.data.single { it.code.lowercase() == code.lowercase() }.let {
           DetailComposable(
               viewModel,
               it,
               code,
               navController,
               loadList = { loadList(navController, viewModel) },
               loadDetail = { navCode -> loadDetail(navController, navCode, viewModel) })
       }
   }
}

fun loadDetail(navController: NavController, code: String, viewModel: DataViewModel) {
    Log.d("Code", code)
    var currentCode = ""
    if(viewModel.backStack.isNotEmpty()) {
        currentCode = viewModel.backStack.last().toString()
    }
    if(currentCode != code) {
        viewModel.updateBackStack(code)
        navController.navigate("guideline/$code") { launchSingleTop = true }
    }
}

fun loadList(navController: NavController, viewModel: DataViewModel) {
    navController.navigate("list")
    viewModel.clearBackStack()
}