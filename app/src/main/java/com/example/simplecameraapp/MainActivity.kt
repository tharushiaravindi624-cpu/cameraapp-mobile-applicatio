
package com.example.simplecameraapp

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.simplecameraapp.ui.theme.SimpleCameraAppCopyTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SimpleCameraAppCopyTheme {
                CameraScreen()
            }
        }
    }
}

@Composable
fun CameraScreen() {

    val context = LocalContext.current

    var capturedImage by remember {
        mutableStateOf<Bitmap?>(null)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedImage = bitmap
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Simple Camera App",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(25.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {

            if (capturedImage != null) {
                Image(
                    bitmap = capturedImage!!.asImageBitmap(),
                    contentDescription = "Captured Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Your captured photo will appear here")
                }
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                cameraLauncher.launch(null)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Camera")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                capturedImage?.let { bitmap ->

                    val imageFile = File(
                        context.cacheDir,
                        "captured_photo.jpg"
                    )

                    FileOutputStream(imageFile).use { output ->
                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            output
                        )
                    }

                    val imageUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        imageFile
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(
                        Intent.createChooser(
                            shareIntent,
                            "Share Photo"
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = capturedImage != null
        ) {
            Text("Share Photo")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                capturedImage?.let { bitmap ->

                    val values = ContentValues().apply {
                        put(
                            MediaStore.Images.Media.DISPLAY_NAME,
                            "CameraPhoto_${System.currentTimeMillis()}.jpg"
                        )
                        put(
                            MediaStore.Images.Media.MIME_TYPE,
                            "image/jpeg"
                        )
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "Pictures/SimpleCameraApp"
                        )
                    }

                    val imageUri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )

                    if (imageUri != null) {
                        val output = context.contentResolver.openOutputStream(imageUri)

                        if (output != null) {
                            output.use {
                                bitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    100,
                                    it
                                )
                            }

                            Toast.makeText(
                                context,
                                "Photo saved to Gallery!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = capturedImage != null
        ) {
            Text("Save to Gallery")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                capturedImage = null

                Toast.makeText(
                    context,
                    "Photo deleted from app!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = capturedImage != null
        ) {
            Text("Delete Photo")
        }
    }
}
