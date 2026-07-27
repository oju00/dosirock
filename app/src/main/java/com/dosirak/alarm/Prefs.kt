package com.dosirak.alarm

import android.content.Context
import java.util.Calendar

/**
 * 알람 on/off 상태와 "도시락을 챙긴 날짜" 기록을 기기 로컬(SharedPreferences)에 저장한다.
 * 서버 없이 기기 안에서만 동작한다.
 */
object Prefs {
    private const val PREF_NAME = "dosirak_prefs"
    private const val KEY_ALARM_ENABLED = "alarm_enabled"
    private const val KEY_CHECKED_DATES = "checked_dates"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun setAlarmEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ALARM_ENABLED, enabled).apply()
    }

    fun isAlarmEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ALARM_ENABLED, false)

    fun markCheckedToday(context: Context) {
        val today = todayString()
        val updated = getCheckedDates(context).toMutableSet()
        updated.add(today)
        prefs(context).edit().putStringSet(KEY_CHECKED_DATES, updated).apply()
    }

    fun getCheckedDates(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_CHECKED_DATES, emptySet()) ?: emptySet()

    fun todayString(): String = formatDate(Calendar.getInstance())

    private fun formatDate(cal: Calendar): String = String.format(
        "%04d-%02d-%02d",
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )

    /** 오늘부터 거꾸로 며칠 연속 체크했는지 계산한다. */
    fun currentStreak(context: Context): Int {
        val dates = getCheckedDates(context)
        val cal = Calendar.getInstance()
        var streak = 0
        while (dates.contains(formatDate(cal))) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    /** 최근 [days]일간의 (날짜, 챙김여부) 목록을 오래된 날짜 -> 오늘 순서로 반환한다. */
    fun recentHistory(context: Context, days: Int = 7): List<Pair<String, Boolean>> {
        val dates = getCheckedDates(context)
        val cal = Calendar.getInstance()
        val result = mutableListOf<Pair<String, Boolean>>()
        for (i in 0 until days) {
            val dateStr = formatDate(cal)
            result.add(dateStr to dates.contains(dateStr))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return result.reversed()
    }
}
