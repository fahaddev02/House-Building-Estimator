package com.example.paintcalculatorestimator.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Static screen shell used by Android Studio previews for ViewModel-backed screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreenPreview(title: String, subtitle: String, fields: List<String>, result: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(fields) { field -> EstimateTextField(value = "", onValueChange = {}, label = { Text(field) }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Calculate") } }
            item { EstimatePanel { Text(result, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) } }
        }
    }
}
