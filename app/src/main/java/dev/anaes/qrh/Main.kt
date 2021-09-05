package dev.anaes.qrh

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("test", detailVm.testData?.items?.get(5)?.head.toString())

        setContent {
            QRHTheme {
                QRH(listVm)
            }
        }
    }

    override fun onBackPressed() {
        if (listVm.searchString.value.text != "" && listVm.onList.value) {
            listVm.searchString.value = TextFieldValue("")
        } else {
            super.onBackPressed()
        }
    }
}

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

@ExperimentalMaterialApi
@Composable
fun NavComposable(navController: NavHostController, modifier: Modifier = Modifier, listVm: ListViewModel){
    NavHost(navController = navController, startDestination = "list", modifier = modifier) {
        composable(route = "list") {
            ListScreen(navController, listVm)
        }
        composable(route = "guideline") {
            DetailScreen(navController)
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun ListScreen(
    navController: NavController,
    listVm: ListViewModel
) {
    ListComposable(listVm)
}

@Composable
fun DetailScreen(navController: NavController) {
    Text("Guideline Screen")
}