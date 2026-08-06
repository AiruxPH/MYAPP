package com.example.act.myapp

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.act.myapp.ui.theme.MYAPPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MYAPPTheme {
                AppNavigation()
            }
        }
    }
}

// 1. Data model for our tracks
data class Track(
    val id: Int,
    val title: String,
    val artist: String,
    val audioUrl: String,
    val imageUrl: String
)

// Sample tracks from Bensound (Royalty Free)
val trackList = listOf(
    Track(1, "Acoustic Breeze", "Bensound", "https://www.bensound.com/bensound-music/bensound-acousticbreeze.mp3", "https://www.bensound.com/bensound-img/acousticbreeze.jpg"),
    Track(2, "Going Higher", "Bensound", "https://www.bensound.com/bensound-music/bensound-goinghigher.mp3", "https://www.bensound.com/bensound-img/goinghigher.jpg"),
    Track(3, "Happy Rock", "Bensound", "https://www.bensound.com/bensound-music/bensound-happyrock.mp3", "https://www.bensound.com/bensound-img/happyrock.jpg"),
    Track(4, "Jazz Comedy", "Bensound", "https://www.bensound.com/bensound-music/bensound-jazzcomedy.mp3", "https://www.bensound.com/bensound-img/jazzcomedy.jpg"),
    Track(5, "Memories", "Bensound", "https://www.bensound.com/bensound-music/bensound-memories.mp3", "https://www.bensound.com/bensound-img/memories.jpg")
)

@Composable
fun AppNavigation() {
    // NavHost and rememberNavController implementation
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController) }
        composable("library/{userName}") { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "Listener"
            LibraryScreen(navController, userName)
        }
        composable("player/{trackId}") { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId")?.toIntOrNull()
            val track = trackList.find { it.id == trackId }
            if (track != null) {
                PlayerScreen(navController, track)
            }
        }
    }
}

@Composable
fun WelcomeScreen(navController: NavController) {
    // User Input Screen
    var name by remember { mutableStateOf("") }
    
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Mood Radio", style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Enter your name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { if (name.isNotBlank()) navController.navigate("library/$name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Start Listening")
            }
        }
    }
}

@Composable
fun LibraryScreen(navController: NavController, userName: String) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            Column(modifier = Modifier.statusBarsPadding().padding(16.dp)) {
                Text("Hello, $userName!", style = MaterialTheme.typography.headlineSmall)
                Text("Select a track to play", style = MaterialTheme.typography.bodyMedium)
            }
        }
    ) { innerPadding ->
        // LazyColumn for scrolling list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(trackList) { track ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            // Toast message
                            Toast.makeText(context, "Loading ${track.title}...", Toast.LENGTH_SHORT).show()
                            navController.navigate("player/${track.id}")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Image Display (AsyncImage from Coil)
                        AsyncImage(
                            model = track.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            // Text Composable
                            Text(track.title, style = MaterialTheme.typography.titleMedium)
                            Text(track.artist, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerScreen(navController: NavController, track: Track) {
    // MediaPlayer implementation
    val mediaPlayer = remember { MediaPlayer() }
    var isPlaying by remember { mutableStateOf(false) }

    // Control MediaPlayer lifecycle
    DisposableEffect(track.audioUrl) {
        mediaPlayer.apply {
            setDataSource(track.audioUrl)
            prepareAsync()
            setOnPreparedListener {
                start()
                isPlaying = true
            }
        }
        onDispose {
            mediaPlayer.release()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = track.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(280.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(track.title, style = MaterialTheme.typography.headlineMedium)
            Text(track.artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer.pause()
                    } else {
                        mediaPlayer.start()
                    }
                    isPlaying = !isPlaying
                },
                modifier = Modifier.width(150.dp)
            ) {
                Text(if (isPlaying) "Pause" else "Play")
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Back to Library")
            }
        }
    }
}
