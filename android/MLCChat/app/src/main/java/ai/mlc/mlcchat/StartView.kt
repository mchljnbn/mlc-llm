package ai.mlc.mlcchat

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartView(navController: NavController, appViewModel: AppViewModel) {
    val localFocusManager = LocalFocusManager.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures { localFocusManager.clearFocus() }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(22.dp))
            Text("Hello,", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Your smart assistant", style = MaterialTheme.typography.displaySmall)
            Text(
                "Choose a model to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
            )
            AssistantOrb(Modifier.align(Alignment.CenterHorizontally))
            Text(
                "Models",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 22.dp, bottom = 10.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(appViewModel.modelList, key = { it.id }) { modelState ->
                    ModelView(navController, modelState, appViewModel)
                }
            }
        }
        if (appViewModel.isShowingAlert()) {
            AlertDialog(
                onDismissRequest = { appViewModel.dismissAlert() },
                onConfirmation = { appViewModel.copyError() },
                error = appViewModel.errorMessage()
            )
        }
    }
}

@Composable
private fun AssistantOrb(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(112.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("✦  ✦", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialog(onDismissRequest: () -> Unit, onConfirmation: () -> Unit, error: String) {
    AlertDialog(
        title = { Text("Something went wrong") },
        text = { Text(error) },
        onDismissRequest = onDismissRequest,
        confirmButton = { TextButton(onClick = onConfirmation) { Text("Copy") } },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("Dismiss") } }
    )
}

@Composable
fun ModelView(navController: NavController, modelState: AppViewModel.ModelState, appViewModel: AppViewModel) {
    var isDeletingModel by rememberSaveable { mutableStateOf(false) }
    val state = modelState.modelInitState.value
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) { Text("AI", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(modelState.modelConfig.modelId, style = MaterialTheme.typography.titleMedium)
                    Text(
                        when (state) {
                            ModelInitState.Finished -> "Ready to chat"
                            ModelInitState.Downloading -> "Downloading model"
                            ModelInitState.Paused -> "Ready to download"
                            else -> "Preparing model"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state == ModelInitState.Paused) {
                    IconButton(onClick = modelState::handleStart) { Icon(Icons.Outlined.Download, "Download") }
                } else if (state == ModelInitState.Downloading) {
                    IconButton(onClick = modelState::handlePause) { Icon(Icons.Outlined.Pause, "Pause") }
                } else if (state == ModelInitState.Finished) {
                    Button(
                        onClick = { modelState.startChat(); navController.navigate("chat") },
                        enabled = appViewModel.chatState.interruptable(),
                        shape = RoundedCornerShape(50),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) { Icon(Icons.Outlined.Chat, null); Spacer(Modifier.width(6.dp)); Text("Chat") }
                } else {
                    Icon(Icons.Outlined.Schedule, "Preparing", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state == ModelInitState.Downloading || state == ModelInitState.Paused || state == ModelInitState.Finished) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { modelState.progress.value.toFloat() / modelState.total.value.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth().clip(CircleShape)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { isDeletingModel = true }) {
                        Icon(Icons.Outlined.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (isDeletingModel) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { isDeletingModel = false }) { Text("Cancel") }
                    TextButton(onClick = { isDeletingModel = false; modelState.handleClear() }) { Text("Clear data") }
                    TextButton(onClick = { isDeletingModel = false; modelState.handleDelete() }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}
