package com.ryota.japanradio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
            JapanRadioScreen()
        }
    }
}

@Composable
fun JapanRadioScreen() {

    var radios by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        try {
            val stationList = withContext(Dispatchers.IO) {

                val url = URL(
                    "https://de1.api.radio-browser.info/json/stations/bycountrycodeexact/JP?hidebroken=true&limit=100"
                )

                val connection = url.openConnection() as HttpURLConnection

                try {
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10000
                    connection.readTimeout = 10000

                    val response = connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                    val jsonArray = JSONArray(response)
                    val result = mutableListOf<RadioStation>()

                    for (i in 0 until jsonArray.length()) {

                        val station = jsonArray.getJSONObject(i)

                        val name = station.optString("name")
                        val streamUrl = station.optString("url_resolved")
                        val favicon = station.optString("favicon")

                        if (name.isNotBlank() && streamUrl.isNotBlank()) {
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

            radios = emptyList()

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
                .padding(
                    top = 20.dp,
                    bottom = 16.dp
                ),
            textAlign = TextAlign.Center
        )

        if (isLoading) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                CircularProgressIndicator()

                Spacer(modifier = Modifier.height(12.dp))

                Text("Loading radio stations...")
            }

        } else {

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(radios) { radio ->
                    RadioCard(radio)
                }
            }
        }
    }
}

@Composable
fun RadioCard(radio: RadioStation) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (radio.favicon.isNotBlank()) {

                AsyncImage(
                    model = radio.favicon,
                    contentDescription = radio.name,
                    modifier = Modifier.size(65.dp),
                    contentScale = ContentScale.Fit
                )

            } else {

                Text(
                    text = "♪",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.size(65.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = radio.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
