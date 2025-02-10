package com.example.chatbot3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatbot3.ui.theme.ChatBot3Theme
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.example.chatbot3.ChatViewModelFactory
import com.example.chatbot3.TogetherAIDataSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatBot3Theme {
                val viewModel: ChatViewModel = viewModel(
                    factory = ChatViewModelFactory(
                        TogetherAIDataSource()
                    )
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { paddingValues ->
                    ChatScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    var userInput by remember { mutableStateOf("") }
    val chatMessages by viewModel.chatMessages.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD)) // Light blue background
    ) {
        // Chat History
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            items(chatMessages, key = { it.text + it.isUser }) { message ->
                ChatBubble(message = message)
            }
        }

        // Input Area with white background
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .windowInsetsPadding(WindowInsets.navigationBars), // Move this here
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .height(144.dp)  // Move fixed height here
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Input TextField with doubled height
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .width(IntrinsicSize.Max)
                        .padding(end = 10.dp)
                        .height(128.dp),
                    placeholder = { Text("Type your message...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(22.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (userInput.isNotBlank()) {
                                viewModel.addUserMessage(userInput)
                                userInput = ""
                            }
                        }
                    )
                )

                // Column for Send and Clear buttons
                Column(
                    modifier = Modifier.height(128.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Send Button
                    Button(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                viewModel.addUserMessage(userInput)
                                userInput = ""
                            }
                        },
                        modifier = Modifier
                            .height(57.dp)
                            .width(82.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0E1FF)
                        ),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Send",
                            color = Color(0xFF1A1A7A),
                            fontSize = 14.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        )
                    }

                    // Clear Button
                    Button(
                        onClick = { 
                            userInput = ""  // Clear input field
                            viewModel.clearAllMessages()  // Clear all chat messages
                        },
                        modifier = Modifier
                            .height(57.dp)
                            .width(82.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0E1FF)
                        ),
                        shape = RoundedCornerShape(22.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Clear",
                            color = Color(0xFF1A1A7A),
                            fontSize = 14.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val backgroundColor = if (message.isUser) Color(0xFFBBDEFB) else Color.White
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        }
    }
}