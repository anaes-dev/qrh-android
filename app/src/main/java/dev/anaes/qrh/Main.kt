package dev.anaes.qrh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.anaes.qrh.ui.list.ListComposable

import dev.anaes.qrh.ui.theme.QRHTheme
import dev.anaes.qrh.vm.DetailViewModel
import dev.anaes.qrh.vm.ListViewModel
import kotlinx.coroutines.CoroutineScope

class Main : ComponentActivity() {

    private val listVm: ListViewModel by viewModels()
    private val detailVm: DetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRHTheme {
                QRHApp()
            }
        }
    }
}

@Preview
@Composable
fun QRHApp() {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState= scaffoldState,
        topBar = { TopBar(scaffoldState, scope) },
    ) { innerPadding ->
        NavHost(navController, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun TopBar(scaffoldState: ScaffoldState, scope: CoroutineScope) {
    TopAppBar(
        title = {
            Text(
                text = "ScaffoldSample",
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
fun NavHost(navController: NavHostController, modifier: Modifier = Modifier){
    NavHost(navController = navController, startDestination = "list", modifier = modifier) {
        composable(route = "list") {
            ListScreen(navController)
        }
        composable(route = "guideline") {
            DetailScreen(navController)
        }
    }
}

@Composable
fun ListScreen(
    navController: NavController,
) {
    ListComposable()
}

@Composable
fun DetailScreen(navController: NavController) {
    Text("Guideline Screen")
}