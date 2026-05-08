package com.example.worknet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.worknet.data.model.Notification
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit
) {
    val isUnread = !notification.read

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable{ onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isUnread)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isUnread) 6.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .padding(bottom = 6.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50)
                        )
                )
            }

            // Titolo
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Messaggio
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badge per distinguere i tipi
            if (notification.type == "response") {
                Text(
                    text = "Risposta",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Info",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
