package ai.mlc.mlcchat

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.jeziellago.compose.markdowntext.MarkdownText

@ExperimentalMaterial3Api
@Composable
fun ChatView(navController: NavController, chatState: AppViewModel.ChatState, activity: Activity) {
    val localFocusManager = LocalFocusManager.current
    val mainActivity = activity as? MainActivity ?: return
    mainActivity.chatState = chatState
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("MLCChat: " + chatState.modelName.value.substringBefore("-"), color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, enabled = chatState.interruptable()) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { chatState.requestResetChat(); mainActivity.hasImage = false }, enabled = chatState.interruptable()) {
                        Icon(Icons.Filled.Replay, "Reset", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        modifier = Modifier.pointerInput(Unit) { detectTapGestures { localFocusManager.clearFocus() } }
    ) { paddingValues ->
        val listState = rememberLazyListState()
        LaunchedEffect(chatState.messages.size) {
            if (chatState.messages.isNotEmpty()) listState.animateScrollToItem(chatState.messages.lastIndex)
        }
        Column(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 14.dp)) {
            Text(chatState.report.value, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(top = 8.dp))
            Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
                state = listState
            ) {
                items(chatState.messages, key = { it.id }) { message -> MessageView(message, mainActivity) }
            }
            Divider(thickness = 1.dp, modifier = Modifier.padding(top = 6.dp))
            SendMessageView(chatState, mainActivity)
        }
    }
}

@Composable
fun MessageView(messageData: MessageData, activity: Activity?) {
    val mainActivity = activity as? MainActivity ?: return
    var useMarkdown by remember { mutableStateOf(true) }
    SelectionContainer {
        if (messageData.role == MessageRole.Assistant) {
            Column {
                if (messageData.text.isNotEmpty()) Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Show as Markdown", color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(end = 8.dp))
                    Switch(checked = useMarkdown, onCheckedChange = { useMarkdown = it })
                }
                Row(Modifier.fillMaxWidth()) {
                    if (useMarkdown) MarkdownText(
                        isTextSelectable = true,
                        modifier = Modifier.wrapContentWidth().background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(18.dp)).padding(10.dp).widthIn(max = 320.dp),
                        markdown = messageData.text
                    ) else Text(messageData.text, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.wrapContentWidth().background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(18.dp)).padding(10.dp).widthIn(max = 320.dp))
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                val uri = messageData.imageUri
                if (uri != null) {
                    val bitmap = runCatching {
                        mainActivity.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
                    }.getOrNull()
                    val displayBitmap = bitmap?.let { Bitmap.createScaledBitmap(it, 224, 224, true) }
                    if (displayBitmap != null) Image(displayBitmap.asImageBitmap(), "Selected image", modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)).padding(6.dp).widthIn(max = 300.dp))
                    if (!mainActivity.hasImage) {
                        mainActivity.chatState.requestImageBitmap(uri)
                        mainActivity.hasImage = true
                    }
                } else Text(messageData.text, textAlign = TextAlign.Right, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.wrapContentWidth().background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)).padding(10.dp).widthIn(max = 320.dp))
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun SendMessageView(chatState: AppViewModel.ChatState, activity: Activity) {
    val localFocusManager = LocalFocusManager.current
    val mainActivity = activity as? MainActivity ?: return
    var text by rememberSaveable { mutableStateOf("") }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(IntrinsicSize.Max).fillMaxWidth().padding(bottom = 8.dp)) {
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Message") }, singleLine = true, modifier = Modifier.weight(1f))
        IconButton(onClick = mainActivity::takePhoto, enabled = chatState.chatable() && !mainActivity.hasImage) { Icon(Icons.Filled.AddAPhoto, "Use camera") }
        IconButton(onClick = mainActivity::pickImageFromGallery, enabled = chatState.chatable() && !mainActivity.hasImage) { Icon(Icons.Filled.Photo, "Select image") }
        IconButton(onClick = {
            val prompt = text.trim()
            if (prompt.isNotEmpty()) { localFocusManager.clearFocus(); chatState.requestGenerate(prompt, mainActivity); text = "" }
        }, enabled = text.isNotBlank() && chatState.chatable()) { Icon(Icons.Filled.Send, "Send message") }
    }
}

@Preview
@Composable
fun MessageViewPreviewWithMarkdown() {
    MessageView(MessageData(MessageRole.Assistant, "# Sample Header\n* Markdown"), null)
}
