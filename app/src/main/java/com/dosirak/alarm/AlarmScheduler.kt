package com.dosirak.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * AlarmManager.setAlarmClock()을 사용해 알람을 예약한다.
 * setAlarmClock()은 진짜 "알람 시계" 앱이 쓰는 API로, Doze(절전) 모드의 영향을 받지 않고
 * 정확한 시각에 울리는 것을 시스템이 보장해준다. 안드로이드 12+ 의 "정확한 알람" 권한 제한도
 * 이 API에는 적용되지 않아 별도 권한 요청이 필요 없다.
 */
object AlarmScheduler {
    private const val REQUEST_CODE_DAILY = 1001
    private const val REQUEST_CODE_SNOOZE = 1002
    private const val REQUEST_CODE_SHOW_INTENT = 1003
    const val EXTRA_IS_SNOOZE = "is_snooze"

    fun scheduleDaily(context: Context, hour: Int = 8, minute: Int = 55) {
        schedule(context, nextTriggerTime(hour, minute), isSnooze = false)
    }

    fun scheduleSnooze(context: Context, minutesFromNow: Int = 5) {
        schedule(context, System.currentTimeMillis() + minutesFromNow * 60_000L, isSnooze = true)
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmPendingIntent(context, isSnooze = false))
        alarmManager.cancel(alarmPendingIntent(context, isSnooze = true))
    }

    private fun schedule(context: Context, triggerAtMillis: Long, isSnooze: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val operation = alarmPendingIntent(context, isSnooze)
        val showIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_SHOW_INTENT,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent)
        alarmManager.setAlarmClock(info, operation)
    }

    private fun alarmPendingIntent(context: Context, isSnooze: Boolean): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_IS_SNOOZE, isSnooze)
        }
        val requestCode = if (isSnooze) REQUEST_CODE_SNOOZE else REQUEST_CODE_DAILY
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerTime(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }
}
