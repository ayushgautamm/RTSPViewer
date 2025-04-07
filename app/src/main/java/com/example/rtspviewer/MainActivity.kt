package com.example.rtspviewer

import android.app.PictureInPictureParams
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.example.rtspviewer.ui.theme.RtspViewerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var libVlc: LibVLC
    private var mediaPlayer: MediaPlayer? = null
    private var videoLayout: VLCVideoLayout? = null

    private var isRecording by mutableStateOf(false)
    private var streamUrl by mutableStateOf("rtsp://10.2.7.38:5540/ch0")
    private var connectionStatus by mutableStateOf("Disconnected")
    private var recordingPath by mutableStateOf("")
    private var isInPipMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vlcOptions = mutableListOf(
            "--rtsp-tcp",
            "--network-caching=300",
            "--avcodec-hw=any",
            "--drop-late-frames",
            "--skip-frames"
        )

        libVlc = LibVLC(this, vlcOptions)
        mediaPlayer = MediaPlayer(libVlc)

        setContent {
            RtspViewerTheme {
                StreamViewerUI()
            }
        }
    }

    @Composable
    private fun StreamViewerUI() {
        val context = LocalContext.current

        if (isInPipMode) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VLCVideoLayout(ctx).also {
                        videoLayout = it
                        mediaPlayer?.attachViews(it, null, false, false)
                    }
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                StreamUrlInput()
                ConnectionStatus()
                ControlButtons(context)
                VideoPreview()
            }
        }
    }

    @Composable
    private fun StreamUrlInput() {
        OutlinedTextField(
            value = streamUrl,
            onValueChange = { streamUrl = it },
            label = { Text("RTSP Stream URL") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    @Composable
    private fun ConnectionStatus() {
        Text(
            text = "Status: $connectionStatus",
            style = MaterialTheme.typography.bodyLarge,
            color = when (connectionStatus) {
                "Connected", "Recording" -> MaterialTheme.colorScheme.primary
                "Error" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        if (isRecording) {
            Text(
                text = "Recording to: $recordingPath",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    @Composable
    private fun ControlButtons(context: Context) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { connectToStream() }) {
                Text("Connect")
            }

            Button(onClick = { disconnectStream() }) {
                Text("Disconnect")
            }

            Button(
                onClick = { toggleRecording(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(if (isRecording) "Stop Recording" else "Record")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { enterPipMode() }) {
                Text("PiP Mode")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    @Composable
    private fun VideoPreview() {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            factory = { context ->
                VLCVideoLayout(context).also {
                    videoLayout = it
                    mediaPlayer?.attachViews(it, null, false, false)
                }
            }
        )
    }

    private fun connectToStream() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                mediaPlayer?.stop()
                connectionStatus = "Connecting..."

                val media = Media(libVlc, Uri.parse(streamUrl)).apply {
                    setHWDecoderEnabled(true, false)
                    addOption(":network-caching=300")
                    addOption(":rtsp-tcp")
                    addOption(":avcodec-hw=any")
                }

                mediaPlayer?.media = media
                media.release()

                mediaPlayer?.setEventListener { event ->
                    when (event.type) {
                        MediaPlayer.Event.Opening -> connectionStatus = "Opening"
                        MediaPlayer.Event.Buffering -> connectionStatus = "Buffering ${event.buffering}%"
                        MediaPlayer.Event.Playing -> connectionStatus = if (isRecording) "Recording" else "Connected"
                        MediaPlayer.Event.EncounteredError -> {
                            connectionStatus = "Error"
                            showToast("Connection failed")
                        }
                        else -> {}
                    }
                }

                mediaPlayer?.play()
            } catch (e: Exception) {
                connectionStatus = "Error"
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun toggleRecording(context: Context) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!isRecording) {
                    startRecording(context)
                } else {
                    stopRecording(context)
                }
            } catch (e: Exception) {
                showToast("Recording error: ${e.message}")
                isRecording = false
                connectionStatus = "Error"
            }
        }
    }

    private fun startRecording(context: Context) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "RTSP_Recording_$timestamp.mp4"
        recordingPath = "${getExternalFilesDir(null)?.absolutePath}/$fileName"

        mediaPlayer?.stop()
        connectionStatus = "Preparing recording..."
        val media = Media(libVlc, Uri.parse(streamUrl)).apply {
            setHWDecoderEnabled(true, false)
            addOption(":network-caching=300")
            addOption(":rtsp-tcp")

            // Send video to both file and display
            addOption(":sout=#duplicate{dst=std{access=file,mux=mp4,dst='$recordingPath'},dst=display}")
            addOption(":sout-keep")
        }


        mediaPlayer?.media = media
        media.release()
        mediaPlayer?.play()

        isRecording = true
        connectionStatus = "Recording"
        showToast("Recording started")
    }

    private fun stopRecording(context: Context) {
        mediaPlayer?.stop()
        isRecording = false
        connectionStatus = "Connected"
        showToast("Recording saved to: $recordingPath")
    }

    private fun disconnectStream() {
        if (isRecording) {
            toggleRecording(this)
        }
        mediaPlayer?.stop()
        connectionStatus = "Disconnected"
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            videoLayout?.post {
                mediaPlayer?.detachViews()
                videoLayout?.let {
                    mediaPlayer?.attachViews(it, null, false, false)
                }

                val pipParams = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(pipParams)
            }
        } else {
            showToast("PiP requires Android 8.0+")
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isInPictureInPictureMode) {
            enterPipMode()
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
        window.decorView.systemUiVisibility = if (isInPipMode) {
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isInPictureInPictureMode) {
            mediaPlayer?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        libVlc.release()
    }
}
