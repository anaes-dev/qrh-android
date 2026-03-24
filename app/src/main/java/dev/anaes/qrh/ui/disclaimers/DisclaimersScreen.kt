package dev.anaes.qrh.ui.disclaimers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.anaes.qrh.R
import dev.anaes.qrh.ui.components.HtmlText
import dev.anaes.qrh.ui.components.htmlStringResource
import dev.anaes.qrh.ui.components.rememberExternalLinkHandler
import dev.anaes.qrh.ui.theme.qrhTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisclaimersScreen(
    onNavigateBack: () -> Unit,
) {
    val onExternalLink = rememberExternalLinkHandler()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_disclaimers)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = qrhTopAppBarColors(),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text(text = stringResource(R.string.about_4), style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            HtmlText(
                html = htmlStringResource(R.string.splash_3),
                style = MaterialTheme.typography.bodySmall,
                onExternalLink = onExternalLink,
            )
            Spacer(modifier = Modifier.height(12.dp))
            HtmlText(
                html = htmlStringResource(R.string.splash_4),
                style = MaterialTheme.typography.bodySmall,
                onExternalLink = onExternalLink,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = stringResource(R.string.splash_5), style = MaterialTheme.typography.bodySmall)
        }
    }
}
