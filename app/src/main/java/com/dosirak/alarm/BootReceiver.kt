package com.dosirak.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 기기가 재부팅되면 AlarmManager에 예약해둔 알람이 모두 사라지기 때문에,
 * 알람이 켜져 있던 상태였다면 여기서 다시 예약해준다.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (Prefs.isAlarmEnabled(context)) {
                AlarmScheduler.scheduleDaily(context)
            }
        }
    }
}
