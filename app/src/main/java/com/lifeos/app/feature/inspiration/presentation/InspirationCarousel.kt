package com.lifeos.app.feature.inspiration.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lifeos.app.core.common.ImageStorage
import com.lifeos.app.feature.inspiration.domain.InspirationItem
import com.lifeos.app.feature.inspiration.domain.InspirationType

@Composable
fun InspirationCarousel(viewModel: InspirationViewModel = hiltViewModel(), modifier: Modifier = Modifier) {
    val items by viewModel.items.collectAsState()
    val context = LocalContext.current
    var showAddChoiceDialog by remember { mutableStateOf(false) }
    var showAddQuoteDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val path = ImageStorage.copyToInternalStorage(context, uri)
            viewModel.addImage(path)
        }
    }

    var itemPendingImageAttach by remember { mutableStateOf<InspirationItem?>(null) }
    val attachImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val pendingItem = itemPendingImageAttach
        if (uri != null && pendingItem != null) {
            val path = ImageStorage.copyToInternalStorage(context, uri)
            viewModel.attachImageToQuote(pendingItem, path)
        }
        itemPendingImageAttach = null
    }

    Card(modifier = modifier.fillMaxWidth().height(180.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No quotes or images yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { items.size })
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    InspirationPage(
                        item = items[page],
                        onDelete = { viewModel.delete(items[page].id) },
                        onLongPressQuote = {
                            itemPendingImageAttach = items[page]
                            attachImagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    )
                }
            }
            IconButton(
                onClick = { showAddChoiceDialog = true },
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add quote or image")
            }
        }
    }

    if (showAddChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showAddChoiceDialog = false },
            title = { Text("Add inspiration") },
            text = { Text("Add a quote you typed, or pick an image from your gallery.") },
            confirmButton = {
                TextButton(onClick = {
                    showAddChoiceDialog = false
                    showAddQuoteDialog = true
                }) {
                    Icon(Icons.Filled.FormatQuote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Quote")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddChoiceDialog = false
                    imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Image")
                }
            },
        )
    }

    if (showAddQuoteDialog) {
        AddQuoteDialog(
            onDismiss = { showAddQuoteDialog = false },
            onConfirm = { text, author ->
                viewModel.addQuote(text, author)
                showAddQuoteDialog = false
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InspirationPage(item: InspirationItem, onDelete: () -> Unit, onLongPressQuote: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        when (item.type) {
            InspirationType.IMAGE -> AsyncImage(
                model = item.imagePath?.let { java.io.File(it) },
                contentDescription = "Inspiration image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
            )
            InspirationType.QUOTE -> {
                val hasImage = item.imagePath != null
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .combinedClickable(onClick = {}, onLongClick = onLongPressQuote),
                ) {
                    if (hasImage) {
                        AsyncImage(
                            model = java.io.File(item.imagePath!!),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                    }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "“${item.text}”",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (hasImage) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (item.author.isNotBlank()) {
                            Text(
                                text = "— ${item.author}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (hasImage) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.BottomEnd)) {
            Icon(Icons.Filled.Delete, contentDescription = "Remove item")
        }
    }
}

@Composable
private fun AddQuoteDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New quote") },
        text = {
            Column {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Quote") })
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author (optional)") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text, author) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
