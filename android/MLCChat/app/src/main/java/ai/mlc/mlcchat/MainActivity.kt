package ai.mlc.mlcchat

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import ai.mlc.mlcchat.ui.theme.MLCChatTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {
    var hasImage = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null || !this::chatState.isInitialized) return@registerForActivityResult
        try {
            contentResolver.takePersistableUriPermission(uri, 0)
        } catch (_: SecurityException) {
            // GetContent grants temporary access; some providers do not support persistence.
        }
        chatState.messages.add(MessageData(MessageRole.User, "", UUID.randomUUID(), uri))
    }

    private var cameraImageUri: Uri? = null
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraImageUri
        if (success && uri != null && this::chatState.isInitialized) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.update(
                    uri,
                    ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                    null,
                    null
                )
            }
            chatState.messages.add(MessageData(MessageRole.User, "", UUID.randomUUID(), uri))
        } else if (uri != null) {
            contentResolver.delete(uri, null, null)
        }
        cameraImageUri = null
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
            else Log.w("MainActivity", "Camera permission was denied")
        }

    lateinit var chatState: AppViewModel.ChatState

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatState = AppViewModel(application).ChatState()
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                MLCChatTheme { NavView(this) }
            }
        }
    }

    fun pickImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        launchCamera()
    }

    private fun launchCamera() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_$timestamp.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MLCChat")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri == null) {
            Log.e("MainActivity", "Could not create image output Uri")
            return
        }
        cameraImageUri = uri
        takePictureLauncher.launch(uri)
    }
}
