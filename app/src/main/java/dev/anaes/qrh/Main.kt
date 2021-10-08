package dev.anaes.qrh

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import dev.anaes.qrh.vm.DetailViewModel
import dev.anaes.qrh.vm.ListViewModel
import kotlinx.coroutines.CoroutineScope


@HiltAndroidApp
class App: Application() {
}

@AndroidEntryPoint
@ExperimentalMaterialApi
class Main : ComponentActivity() {
    private val listVm: ListViewModel by viewModels()
    private val detailVm: DetailViewModel by viewModels()

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            QRHTheme {
                QRH(listVm)
            }
        }
    }

    override fun onBackPressed() {
        if (listVm.searchString.value != "" && listVm.onList.value) {
            listVm.updateSearch("")
        } else {
            super.onBackPressed()
        }
    }
}

@ExperimentalFoundationApi
@Composable
@ExperimentalMaterialApi
fun QRH(listVm: ListViewModel) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState= scaffoldState,
        topBar = { TopBar(scaffoldState, scope) },
    ) { innerPadding ->
        NavComposable(navController, modifier = Modifier.padding(innerPadding), listVm)
    }
}

@Composable
fun TopBar(scaffoldState: ScaffoldState, scope: CoroutineScope) {
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

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun NavComposable(navController: NavHostController, modifier: Modifier = Modifier, listVm: ListViewModel){
    NavHost(navController = navController, startDestination = "list", modifier = modifier) {
        composable(route = "list") {
            ListScreen(navController, listVm)
        }
        composable(route = "guideline/{code}") {
            DetailScreen(navController, listVm, it.arguments!!.getString("code")!!)
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun ListScreen(
    navController: NavController,
    listVm: ListViewModel
) {
    ListComposable(listVm) {
        navController.navigate("guideline/$it")
    }
}

@Composable
fun DetailScreen(
    navController: NavController,
    listVm: ListViewModel,
    code: String
) {
    DetailComposable(listVm, code)
}