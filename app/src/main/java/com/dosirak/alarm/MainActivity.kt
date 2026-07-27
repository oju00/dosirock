package com.dosirak.alarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var switchAlarm: Switch
    private lateinit var textStreak: TextView
    private lateinit var textStatus: TextView
    private lateinit var historyContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switchAlarm = findViewById(R.id.switchAlarm)
        textStreak = findViewById(R.id.textStreak)
        textStatus = findViewById(R.id.textStatus)
        historyContainer = findViewById(R.id.historyContainer)

        switchAlarm.isChecked = Prefs.isAlarmEnabled(this)

        switchAlarm.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestNotificationPermissionIfNeeded()
                Prefs.setAlarmEnabled(this, true)
                AlarmScheduler.scheduleDaily(this)
                Toast.makeText(this, "매일 아침 8시 55분에 알람이 울려요", Toast.LENGTH_SHORT).show()
            } else {
                Prefs.setAlarmEnabled(this, false)
                AlarmScheduler.cancel(this)
                Toast.makeText(this, "알람을 껐어요", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.buttonTest).setOnClickListener {
            val intent = Intent(this, AlarmActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(AlarmActivity.EXTRA_MESSAGE, Messages.random())
            }
            startActivity(intent)
        }

        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun refreshUi() {
        val streak = Prefs.currentStreak(this)
        textStreak.text = "연속 ${streak}일째 도시락 챙기는 중"

        textStatus.text = if (Prefs.getCheckedDates(this).contains(Prefs.todayString())) {
            "오늘은 이미 도시락을 챙겼어요"
        } else {
            "오늘은 아직 체크 전이에요"
        }

        historyContainer.removeAllViews()
        val history = Prefs.recentHistory(this, 7)
        for ((date, checked) in history) {
            val label = TextView(this)
            val day = date.substring(5) // MM-DD
            label.text = "$day\n${if (checked) "O" else "-"}"
            label.gravity = android.view.Gravity.CENTER
            label.setPadding(12, 8, 12, 8)
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.weight = 1f
            label.layoutParams = params
            historyContainer.addView(label)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
}
