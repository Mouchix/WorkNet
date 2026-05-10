package com.example.worknet.data.repository

import com.example.worknet.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun notificationsCollection(userId: String) =
        db.collection("users")
            .document(userId)
            .collection("notifications")

    // ---------------------------------------------------------
    // CREAZIONE NOTIFICA
    // ---------------------------------------------------------
    suspend fun createNotification(userId: String, notification: Notification) {
        notificationsCollection(userId)
            .document(notification.id)
            .set(notification)
            .await()
    }

    // ---------------------------------------------------------
    // LETTURA NOTIFICHE (una tantum)
    // ---------------------------------------------------------
    suspend fun getNotifications(userId: String): List<Notification> {
        val snapshot = notificationsCollection(userId)
            .orderBy("createdAt")
            .get()
            .await()

        return snapshot.toObjects(Notification::class.java)
    }

    // ---------------------------------------------------------
    // LETTURA IN TEMPO REALE (Flow)
    // ---------------------------------------------------------
    fun observeNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = notificationsCollection(userId)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.toObjects(Notification::class.java) ?: emptyList()
                trySend(notifications)
            }

        awaitClose { listener.remove() }
    }

    // ---------------------------------------------------------
    // SEGNARE COME LETTA
    // ---------------------------------------------------------
    suspend fun markAsRead(userId: String, notificationId: String) {
        notificationsCollection(userId)
            .document(notificationId)
            .update("read", true)
            .await()
    }

    // ---------------------------------------------------------
    // ELIMINAZIONE NOTIFICA
    // ---------------------------------------------------------
    suspend fun deleteNotification(userId: String, notificationId: String) {
        notificationsCollection(userId)
            .document(notificationId)
            .delete()
            .await()
    }

    // ---------------------------------------------------------
    // ELIMINARE TUTTE LE NOTIFICHE (opzionale)
    // ---------------------------------------------------------
    suspend fun clearAllNotifications(userId: String) {
        val snapshot = notificationsCollection(userId).get().await()
        snapshot.documents.forEach { it.reference.delete().await() }
    }

    fun observeUnreadNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = notificationsCollection(userId)
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(Notification::class.java) ?: emptyList()
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

}
