package com.example.ai.tools

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent

class MyNotificationListenerService : NotificationListenerService() {

    companion object {
        var instance: MyNotificationListenerService? = null
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        // Can be used to read notifications
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    fun getRecentNotifications(): List<String> {
        val result = mutableListOf<String>()
        try {
            val notifications = activeNotifications
            for (sbn in notifications) {
                val pkg = sbn.packageName
                val extras = sbn.notification.extras
                val title = extras.getString(android.app.Notification.EXTRA_TITLE) ?: ""
                val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
                if (title.isNotEmpty() || text.isNotEmpty()) {
                    result.add("From $pkg: $title - $text")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun clearAll() {
        try {
            cancelAllNotifications()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
