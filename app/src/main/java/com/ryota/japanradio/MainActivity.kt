package com.ryota.japanradio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class RadioStation(
    val name: String,
    val streamUrl: String,
    val favicon: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                JapanRadioScreen()
            }
        }
    }
}

@Composable
fun JapanRadioScreen() {
    var radios by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    var currentRadio by remember { mutableStateOf<RadioStation?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isPreparing by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }
    var playerError by remember { mutableStateOf("") }

    val mediaPlayer = remember {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }

    fun playRadio(radio: RadioStation) {
        playerError = ""

        if (currentRadio?.streamUrl == radio.streamUrl && isPrepared) {
            if (isPlaying) {
                mediaPlayer.pause()
                isPlaying = false
            } else {
                mediaPlayer.start()
                isPlaying = true
            }
            return
        }

        try {
            mediaPlayer.reset()

            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            currentRadio = radio
            isPlaying = false
            isPrepared = false
            isPreparing = true

            mediaPlayer.setDataSource(radio.streamUrl)

            mediaPlayer.setOnPreparedListener { player ->
                isPreparing = false
                isPrepared = true
                player.start()
                isPlaying = true
            }

            mediaPlayer.setOnCompletionListener {
                isPlaying = false
            }

            mediaPlayer.setOnErrorListener { _, _, _ ->
                isPreparing = false
                isPrepared = false
                isPlaying = false
                playerError = "Stream tidak dapat diputar. Coba radio lain."
                true
            }

            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            isPreparing = false
            isPrepared = false
            isPlaying = false
            playerError = "Stream tidak dapat diputar: ${e.message}"
        }
    }

    LaunchedEffect(Unit) {
        try {
            val stationList = withContext(Dispatchers.IO) {
                val url = URL(
                    "https://de1.api.radio-browser.info/json/stations/bycountrycodeexact/JP?hidebroken=true&limit=405"
                )

                val connection = url.openConnection() as HttpURLConnection

                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.requestMethod = "GET"

                try {
                    val response = connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                    val jsonArray = JSONArray(response)
                    val result = mutableListOf<RadioStation>()

                    for (i in 0 until jsonArray.length()) {
                        val station = jsonArray.getJSONObject(i)

                        val name = station.optString("name").trim()
                        val streamUrl = station.optString("url_resolved").trim()
                        val favicon = station.optString("favicon").trim()

                        if (name.isNotEmpty() && streamUrl.isNotEmpty()) {
                            result.add(
                                RadioStation(
                                    name = name,
                                    streamUrl = streamUrl,
                                    favicon = favicon
                                )
                            )
                        }
                    }

                    result
                } finally {
                    connection.disconnect()
                }
            }

            radios = stationList
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(
            text = "Japan Radio",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // PLAYER SECTION - ALWAYS VISIBLE (FIX: ini yang bermasalah!)
        if (currentRadio != null) {
            currentRadio?.let { radio ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Now Playing:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = radio.name,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = when {
                            playerError.isNotEmpty() -> playerError
                            isPreparing -> "Memuat stream..."
                            isPlaying -> "▶ Sedang diputar"
                            else -> "⏸ Dijeda"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Button(
                        onClick = { playRadio(radio) },
                        enabled = !isPreparing,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(if (isPlaying) "⏸ Jeda" else "▶ Putar")
                    }
                }
            }
        } else {
            // PLACEHOLDER PLAYER WHEN NO STATION SELECTED
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pilih stasiun radio untuk mulai mendengarkan",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // CONTENT SECTION - LOADING / ERROR / STATIONS LIST
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Memuat stasiun radio...")
                }
            }

            errorMessage.isNotEmpty() -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Gagal memuat stasiun radio",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = errorMessage,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            radios.isEmpty() -> {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Tidak ada stasiun radio yang ditemukan.")
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(radios) { radio ->
                        RadioCard(
                            radio = radio,
                            isSelected = currentRadio?.streamUrl == radio.streamUrl,
                            onClick = { playRadio(radio) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RadioCard(
    radio: RadioStation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clickable(onClick = onClick),
        shape = androidx.compose.material3.RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else Color.Transparent
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = radio.favicon,
                contentDescription = radio.name,
                modifier = Modifier.size(65.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = radio.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (isSelected) {
                Text(
                    text = "▶ Playing",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
