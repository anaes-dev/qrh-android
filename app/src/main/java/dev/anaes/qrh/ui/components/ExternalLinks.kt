package dev.anaes.qrh.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import dev.anaes.qrh.R

/**
 * Opens a web link in the browser, or a `tel:` link in the dialler. Shows a toast instead of
 * crashing when no installed app can handle it (e.g. no browser, or a tablet without a dialler).
 */
fun Context.openExternalLink(url: String) {
    val uri = url.toUri()
    val action = if (uri.scheme == "tel") Intent.ACTION_DIAL else Intent.ACTION_VIEW
    try {
        startActivity(Intent(action, uri))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, R.string.no_app_for_link, Toast.LENGTH_SHORT).show()
    }
}

/** Composable-friendly handle on [openExternalLink] for passing as an `onExternalLink` callback. */
@Composable
fun rememberExternalLinkHandler(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) { { url: String -> context.openExternalLink(url) } }
}
