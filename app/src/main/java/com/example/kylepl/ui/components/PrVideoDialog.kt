package com.example.kylepl.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/** Full-screen, edge-to-edge video player dialog for a lift's attached PR clip. */
@Composable
fun PrVideoDialog(
    liftLabel: String,
    videoUri: Uri?,
    onDismiss: () -> Unit,
    onPickVideo: () -> Unit,
    onRemoveVideo: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        HideSystemBarsWhilePresent()

        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (videoUri != null) {
                    PrVideoPlayer(uri = videoUri, modifier = Modifier.fillMaxSize())
                } else {
                    EmptyVideoState(onPickVideo = onPickVideo, modifier = Modifier.align(Alignment.Center))
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "$liftLabel PR",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (videoUri != null) {
                            TextButton(onClick = onPickVideo) { Text("Replace", color = Color.White) }
                            TextButton(onClick = onRemoveVideo) { Text("Remove", color = Color.White) }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyVideoState(onPickVideo: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.VideoLibrary,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.White,
        )
        Text(
            "No video attached yet",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
        Button(onClick = onPickVideo) { Text("Choose Video") }
    }
}

@Composable
private fun PrVideoPlayer(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        },
    )
}

/** Hides the status/nav bars for as long as this dialog is on screen, restoring them on close. */
@Composable
private fun HideSystemBarsWhilePresent() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.parent as? DialogWindowProvider)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }
}
