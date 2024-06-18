package com.dgopadakak.speechtotext

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dgopadakak.speechtotext.ui.theme.SpeechToTextTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpeechToTextTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LayoutWithGoogleSpeechService(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color.Black)
                    )

                    LayoutWithSpeechRecognizer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun LayoutWithGoogleSpeechService(
    modifier: Modifier = Modifier
) {
    val text = remember { mutableStateOf("") }
    val intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Say something"
            )
        }
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            ) as ArrayList<String>
            text.value = results[0]
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(text = text.value.ifEmpty { "Click the button, text will be here" })
        Button(
            onClick = {
                recordAudioLauncher.launch(intent)
            }
        ) {
            Text(text = "Listen with Google Speech Service")
        }
    }
}

@Composable
fun LayoutWithSpeechRecognizer(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val speechToTextParser = remember { SpeechToTextParser(context) }
    val havePermission = remember { mutableStateOf(false) }
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        havePermission.value = isGranted
    }
    val state by speechToTextParser.state.collectAsState()

    LaunchedEffect(key1 = havePermission.value) {
        if (havePermission.value) {
            speechToTextParser.startListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        if (state.isSpeaking) {
            Text(text = "Speaking...")
        } else {
            Text(text = state.spokenText.ifEmpty { "Click the button, text will be here" })
        }
        Button(
            onClick = {
                if (!havePermission.value) {
                    recordAudioLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                } else {
                    if (state.isSpeaking) {
                        speechToTextParser.stopListening()
                    } else {
                        speechToTextParser.startListening()
                    }
                }
            }
        ) {
            Text(text = "Listen with SpeechRecognizer")
        }
    }
}