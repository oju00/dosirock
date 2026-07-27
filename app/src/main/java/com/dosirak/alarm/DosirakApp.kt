package com.dosirak.alarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

class DosirakApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val alarmSoundUri = RingtoneManager.getActualDefaultRingtoneUri(
                this,
                RingtoneManager.TYPE_ALARM
            )

            val channel = NotificationChannel(
                CHANNEL_ID,
                "도시락 알람",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "매일 아침 도시락 챙기기 알람"
                enableVibration(true)
                if (alarmSoundUri != null) {
                    setSound(alarmSoundUri, alarmAttributes)
                }
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "dosirak_alarm_channel"
    }
}
