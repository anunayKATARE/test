package com.lifeos.app.core.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun MoreScreen(onItemClick: (String) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("More") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxWidth()) {
            items(moreMenuItems, key = { it.route }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    onClick = { onItemClick(item.route) },
                ) {
                    Text(text = item.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
