package dev.anaes.qrh.ui.firstrun

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.BuildConfig
import dev.anaes.qrh.R
import dev.anaes.qrh.ui.components.HtmlText
import dev.anaes.qrh.ui.theme.LaunchBtnBgDisabledLight
import dev.anaes.qrh.ui.theme.LaunchBtnBgEnabledLight
import dev.anaes.qrh.ui.theme.LaunchBtnBgExitLight
import dev.anaes.qrh.ui.theme.LaunchBtnTxtDisabledLight
import dev.anaes.qrh.ui.theme.LaunchBtnTxtEnabledLight
import dev.anaes.qrh.ui.theme.LocalIsDarkTheme
import dev.anaes.qrh.ui.theme.LaunchBtnBgDisabledDark
import dev.anaes.qrh.ui.theme.LaunchBtnBgEnabledDark
import dev.anaes.qrh.ui.theme.LaunchBtnBgExitDark
import dev.anaes.qrh.ui.theme.LaunchBtnTxtDisabledDark
import dev.anaes.qrh.ui.theme.LaunchBtnTxtEnabledDark
import kotlinx.coroutines.launch

@Composable
fun FirstRunScreen(
    isUpdate: Boolean,
    onAgree: () -> Unit,
    onExit: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = LocalIsDarkTheme.current
    val scrollFirstText = stringResource(R.string.please_scroll_first)

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

    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onExit,
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
                                snackbarHostState.showSnackbar(scrollFirstText)
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
                .padding(16.dp),
        ) {
            Text(
                text = "QRH",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.long_name),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isUpdate) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.splash_1b),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Text(
                    text = stringResource(R.string.splash_1),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = stringResource(R.string.splash_2a), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_2b),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
            )
            Spacer(modifier = Modifier.height(12.dp))
            HtmlText(
                html = stringResource(R.string.splash_3),
                style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize, lineHeight = MaterialTheme.typography.bodyMedium.lineHeight),
                onExternalLink = { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
            HtmlText(
                html = stringResource(R.string.splash_4),
                style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize, lineHeight = MaterialTheme.typography.bodyMedium.lineHeight),
                onExternalLink = { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = stringResource(R.string.splash_5), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = stringResource(R.string.splash_6), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.splash_7),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.about_2),
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
