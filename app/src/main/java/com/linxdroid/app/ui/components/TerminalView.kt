package com.linxdroid.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.linxdroid.app.model.TerminalLine
import com.linxdroid.app.ui.theme.TerminalBg
import com.linxdroid.app.ui.theme.TerminalPrompt
import com.linxdroid.app.ui.theme.TerminalSystem
import com.linxdroid.app.ui.theme.TerminalTextStyle
import kotlinx.coroutines.launch

@Composable
fun TerminalView(
    lines: List<TerminalLine>,
    onCommand: (String) -> Unit,
    sessionActive: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(lines.size - 1) }
        }
    }

    Column(modifier = modifier.background(TerminalBg)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(lines) { line ->
                Text(
                    text = line.text,
                    style = TerminalTextStyle,
                    color = line.displayColor()
                )
            }
        }

        HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                style = TerminalTextStyle,
                color = TerminalPrompt
            )
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                enabled = sessionActive,
                textStyle = TerminalTextStyle.copy(color = Color.White),
                placeholder = {
                    Text(
                        "Enter command…",
                        style = TerminalTextStyle,
                        color = TerminalSystem
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor  = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor  = Color.Transparent,
                    cursorColor             = TerminalPrompt
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (input.isNotBlank() && sessionActive) {
                        onCommand(input.trim())
                        input = ""
                    }
                })
            )
            IconButton(
                onClick = {
                    if (input.isNotBlank() && sessionActive) {
                        onCommand(input.trim())
                        input = ""
                    }
                },
                enabled = sessionActive && input.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (sessionActive && input.isNotBlank()) TerminalPrompt else TerminalSystem
                )
            }
        }
    }
}
