package com.ryota.japanradio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class RadioStation(
    val name: String,
    val logoRes: Int
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

    val radios = listOf(
        RadioStation("Tokyo FM", android.R.drawable.ic_media_play),
        RadioStation("J-WAVE", android.R.drawable.ic_media_play),
        RadioStation("Radio Osaka", android.R.drawable.ic_media_play),
        RadioStation("FM Yokohama", android.R.drawable.ic_media_play),
        RadioStation("NHK Radio", android.R.drawable.ic_media_play),
        RadioStation("ZIP-FM", android.R.drawable.ic_media_play),
        RadioStation("FM802", android.R.drawable.ic_media_play),
        RadioStation("InterFM", android.R.drawable.ic_media_play),
        RadioStation("BayFM", android.R.drawable.ic_media_play)
    )

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

            Image(
                painter = painterResource(id = radio.logoRes),
                contentDescription = radio.name,
                modifier = Modifier.size(65.dp)
            )

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
