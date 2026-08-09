package com.example.ai.tools

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.MediaStore
import android.app.SearchManager
import android.provider.Settings
import android.widget.Toast

class DeviceControlManager(private val context: Context) {

    /**
     * Tries to open an application by its package name.
     */
    fun openApp(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            false
        }
    }

    /**
     * Plays music using the default media player or search intent.
     * For example, "Play [Song Name] on YouTube/Spotify"
     */
    fun playMusic(query: String, packageName: String? = null) {
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)
            putExtra(SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        if (packageName != null) {
            intent.setPackage(packageName) // e.g., "com.google.android.youtube" or "com.spotify.music"
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to a general web search if no music app can handle it
            searchWeb(query)
        }
    }

    /**
     * Opens a specific settings screen.
     */
    fun openSettings(action: String = Settings.ACTION_SETTINGS) {
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * Opens a web search for a query.
     */
    fun searchWeb(query: String) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * 1. Sets an alarm.
     */
    fun setAlarm(message: String, hour: Int, minutes: Int) {
        val intent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, message)
            putExtra(android.provider.AlarmClock.EXTRA_HOUR, hour)
            putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minutes)
            putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * 2. Sets a timer.
     */
    fun setTimer(message: String, lengthSeconds: Int) {
        val intent = Intent(android.provider.AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, message)
            putExtra(android.provider.AlarmClock.EXTRA_LENGTH, lengthSeconds)
            putExtra(android.provider.AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * 3. Opens phone dialer with a specific number.
     */
    fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * 4. Opens SMS app with a specific number and message.
     */
    fun composeSms(phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * 5. Opens Email app to compose an email.
     */
    fun composeEmail(addresses: Array<String>, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * 6. Opens Map app for navigation.
     */
    fun getDirections(location: String) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$location")
        val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to generic maps
            val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$location")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (genericIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(genericIntent)
            }
        }
    }

    /**
     * 7. Opens Camera app to capture an image.
     */
    fun capturePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * 8. Toggles the flashlight.
     */
    fun toggleFlashlight(on: Boolean) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, on)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 9. Sets the media volume (0-100 percentage).
     */
    fun setVolume(percentage: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
            val targetVolume = (maxVolume * (percentage.coerceIn(0, 100)) / 100f).toInt()
            audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, targetVolume, android.media.AudioManager.FLAG_SHOW_UI)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 10. Gets battery percentage.
     */
    fun getBatteryPercentage(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }
    }

    /**
     * 11. Gets available storage in megabytes.
     */
    fun getAvailableStorageMb(): Long {
        val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)
        val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
        return bytesAvailable / (1024 * 1024)
    }

    /**
     * 12. Opens Wi-Fi Settings.
     */
    fun openWifiSettings() {
        openSettings(Settings.ACTION_WIFI_SETTINGS)
    }

    /**
     * 13. Opens Bluetooth Settings.
     */
    fun openBluetoothSettings() {
        openSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
    }

    /**
     * 14. Goes to Home screen.
     */
    fun goHome() {
        com.example.ai.tools.MyAccessibilityService.instance?.performAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
    }

    /**
     * 15. Goes Back.
     */
    fun goBack() {
        com.example.ai.tools.MyAccessibilityService.instance?.performAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
    }

    /**
     * 16. Opens Recent Apps.
     */
    fun openRecents() {
        com.example.ai.tools.MyAccessibilityService.instance?.performAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS)
    }

    /**
     * 17. Takes a screenshot (Android 9+).
     */
    fun takeScreenshot() {
        com.example.ai.tools.MyAccessibilityService.instance?.performAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
    }

    /**
     * 18. Locks the screen.
     */
    fun lockScreen() {
        try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            val componentName = android.content.ComponentName(context, com.example.ai.tools.MyAdminReceiver::class.java)
            if (devicePolicyManager.isAdminActive(componentName)) {
                devicePolicyManager.lockNow()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 19. Sets screen brightness.
     */
    fun setBrightness(percentage: Int) {
        try {
            if (Settings.System.canWrite(context)) {
                val brightness = (percentage.coerceIn(0, 100) * 255) / 100
                Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 20. Reads active notifications.
     */
    fun readNotifications(): String {
        val notifications = com.example.ai.tools.MyNotificationListenerService.instance?.getRecentNotifications()
        return if (notifications.isNullOrEmpty()) {
            "You have no new notifications."
        } else {
            "You have ${notifications.size} notifications. " + notifications.joinToString(". ")
        }
    }

    /**
     * 21. Clears all notifications.
     */
    fun clearNotifications() {
        com.example.ai.tools.MyNotificationListenerService.instance?.clearAll()
    }

    /**
     * 22. Reads from clipboard.
     */
    fun readClipboard(): String {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            if (clipboard.hasPrimaryClip() && clipboard.primaryClip != null && clipboard.primaryClip!!.itemCount > 0) {
                clipboard.primaryClip!!.getItemAt(0).text?.toString() ?: "Clipboard is empty."
            } else {
                "Clipboard is empty."
            }
        } catch (e: Exception) {
            "Unable to read clipboard."
        }
    }

    /**
     * 23. Writes to clipboard.
     */
    fun setClipboard(text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Assistant Copied Text", text)
            clipboard.setPrimaryClip(clip)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 24. Opens Quick Settings panel.
     */
    fun openQuickSettings() {
        com.example.ai.tools.MyAccessibilityService.instance?.performAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
    }
}
