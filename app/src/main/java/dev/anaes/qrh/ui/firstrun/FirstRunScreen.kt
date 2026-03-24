package dev.anaes.qrh.ui.firstrun

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.R
import dev.anaes.qrh.ui.components.HeaderCard
import dev.anaes.qrh.ui.theme.LaunchBtnBgDisabledDark
import dev.anaes.qrh.ui.theme.LaunchBtnBgDisabledLight
import dev.anaes.qrh.ui.theme.LaunchBtnBgEnabledDark
import dev.anaes.qrh.ui.theme.LaunchBtnBgEnabledLight
import dev.anaes.qrh.ui.theme.LaunchBtnBgExitDark
import dev.anaes.qrh.ui.theme.LaunchBtnBgExitLight
import dev.anaes.qrh.ui.theme.LaunchBtnTxtDisabledDark
import dev.anaes.qrh.ui.theme.LaunchBtnTxtDisabledLight
import dev.anaes.qrh.ui.theme.LaunchBtnTxtEnabledDark
import dev.anaes.qrh.ui.theme.LaunchBtnTxtEnabledLight
import dev.anaes.qrh.ui.theme.LocalIsDarkTheme
import dev.anaes.qrh.ui.theme.RedBgDark
import dev.anaes.qrh.ui.theme.RedBgLight
import dev.anaes.qrh.ui.theme.RedTxtDark
import dev.anaes.qrh.ui.theme.RedTxtLight
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@Composable
fun FirstRunScreen(
    isUpdate: Boolean,
    onAgree: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current

    val hasScrolledToBottom by remember {
        derivedStateOf {
            val maxScroll = scrollState.maxValue
            maxScroll == 0 || scrollState.value >= maxScroll - 50
        }
    }

    val btnBgEnabled = if (isDark) LaunchBtnBgEnabledDark else LaunchBtnBgEnabledLight
    val btnBgDisabled = if (isDark) LaunchBtnBgDisabledDark else LaunchBtnBgDisabledLight
    val btnBgExit = if (isDark) LaunchBtnBgExitDark else LaunchBtnBgExitLight
    val btnTxtEnabled = if (isDark) LaunchBtnTxtEnabledDark else LaunchBtnTxtEnabledLight
    val btnTxtDisabled = if (isDark) LaunchBtnTxtDisabledDark else LaunchBtnTxtDisabledLight

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { exitProcess(1) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = btnBgExit,
                        contentColor = btnTxtDisabled,
                    ),
                ) {
                    Text(stringResource(R.string.exit))
                }

                Button(
                    onClick = {
                        if (hasScrolledToBottom) {
                            onAgree()
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please scroll and read first.")
                            }
                        }
                    },
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasScrolledToBottom) btnBgEnabled else btnBgDisabled,
                        contentColor = if (hasScrolledToBottom) btnTxtEnabled else btnTxtDisabled,
                    ),
                ) {
                    Text(stringResource(R.string.agree_amp_continue))
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(8.dp),
        ) {
            // Header card
            HeaderCard(modifier = Modifier.padding(8.dp))

            // Update notice
            if (isUpdate) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.app_update),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // App description + CC logo (qrh_info section)
            Text(
                text = stringResource(R.string.about_1),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.by_nc_sa),
                    contentDescription = stringResource(R.string.CCBYNCSA),
                    modifier = Modifier
                        .width(120.dp)
                        .padding(8.dp)
                        .clickable {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://creativecommons.org/licenses/by-nc-sa/4.0/")
                                )
                            )
                        },
                )
                Text(
                    text = stringResource(R.string.about_2),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(8.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Intro text
            if (isUpdate) {
                Text(
                    text = stringResource(R.string.splash_1b),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            } else {
                Text(
                    text = stringResource(R.string.splash_1),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Disclaimers with numbered steps
            // Step 1
            DisclaimerStep(number = "1", isDark = isDark) {
                Text(
                    text = stringResource(R.string.splash_2a),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp),
                )
                Text(
                    text = stringResource(R.string.splash_2b),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(8.dp),
                )
                // Red disclaimer card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) RedBgDark else RedBgLight,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.about_4),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) RedTxtDark else RedTxtLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }

            // Step 2
            DisclaimerStep(number = "2", isDark = isDark) {
                Text(
                    text = stringResource(R.string.splash_3),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp),
                )
            }

            // Step 3
            DisclaimerStep(number = "3", isDark = isDark) {
                Text(
                    text = stringResource(R.string.splash_4),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp),
                )
            }

            // Step 4
            DisclaimerStep(number = "4", isDark = isDark) {
                Text(
                    text = stringResource(R.string.splash_5),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp),
                )
            }

            // Step 5
            DisclaimerStep(number = "5", isDark = isDark) {
                Text(
                    text = stringResource(R.string.splash_6),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.splash_7),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DisclaimerStep(
    number: String,
    isDark: Boolean,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}
