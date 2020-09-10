package com.mikiller.sleepnow

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask


class MainActivity : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.simpleName
    private lateinit var receiver: KeyReceiver
    private lateinit var timer: Timer
    private var timeLeft: Long = 60 * 1000
    private var startTimeHour: String = "0"
    private var startTimeMin: String = "0"
    private var endTimeHour: String = "0"
    private var endTimeMin: String = "0"
    private var calander = Calendar.getInstance()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(0x80000000.toInt(), 0x80000000.toInt())
        window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.setOnApplyWindowInsetsListener { v, insets ->
            Log.e(TAG, "window insets")
            insets.consumeSystemWindowInsets()

        }
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        }

        getDefaultSetting()
        setAlarm()
        if (isLock()) {
            toggleTimeSetter(false)
            startLockTask()
        } else{
            toggleTimeSetter(true)
        }
        receiver = KeyReceiver(this, timeLeft, calander.timeInMillis)
        var intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        registerReceiver(receiver, intentFilter)

        edtStartHour.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s?.trim().isNullOrEmpty() && s.toString().toInt() < 24) {
                    startTimeHour = s?.trim().toString()
                    MXPreferenceUtils.getInstance().startTime = "$startTimeHour:${edtStartMin.text}"
                    setAlarm()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        edtStartMin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s?.trim().isNullOrEmpty() && s.toString().toInt() < 60) {
                    startTimeMin = s?.trim().toString()
                    MXPreferenceUtils.getInstance().startTime = "${edtStartHour.text}:$startTimeMin"
                    setAlarm()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        edtEndHour.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s?.trim().isNullOrEmpty() && s.toString().toInt() < 24) {
                    endTimeHour = s?.trim().toString()
                    MXPreferenceUtils.getInstance().endTime = "$endTimeHour:${edtEndMin.text}"
                    setAlarm()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        edtEndMin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s?.trim().isNullOrEmpty() && s.toString().toInt() < 60) {
                    endTimeMin = s?.trim().toString()
                    MXPreferenceUtils.getInstance().endTime = "${edtEndHour.text}:$endTimeMin"
                    setAlarm()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        startTimer()
    }

    private fun getDefaultSetting() {
        var startTime = MXPreferenceUtils.getInstance(this, "SleepNow").startTime
        var endTime = MXPreferenceUtils.getInstance().endTime
        startTimeHour = startTime.substring(0, startTime.indexOf(":"))
        startTimeMin = startTime.substring(startTime.indexOf(":") + 1)
        endTimeHour = endTime.substring(0, endTime.indexOf(":"))
        endTimeMin = endTime.substring(endTime.indexOf(":") + 1)
        edtStartHour.setText(startTimeHour)
        edtStartMin.setText(startTimeMin)
        edtEndHour.setText(endTimeHour)
        edtEndMin.setText(endTimeMin)
    }

    private fun setAlarm() {
        var hour = (endTimeHour.let {
            if (it.toLong() < startTimeHour.toLong()) (it.toLong() + 24L) else it.toLong()
        } - startTimeHour.toLong()) * 3600000
        var min = (endTimeMin.let {
            if (it.toLong() < startTimeMin.toLong()) (it.toLong() + 60) else it.toLong()
        } - startTimeMin.toLong()) * 60000
        timeLeft = hour + min
        Log.e(TAG, "timeleft: $timeLeft")

        Log.e(TAG, "current mill: ${calander.timeInMillis}")
        calander.set(Calendar.HOUR_OF_DAY, startTimeHour.toInt())
        calander.set(Calendar.MINUTE, startTimeMin.toInt())
        Log.e(TAG, "new mill: ${calander.timeInMillis}, sys mill : ${System.currentTimeMillis()}")
        var sdf = SimpleDateFormat("HH:mm:ss")
        Log.e(
            TAG,
            "calend: ${sdf.format(Date(calander.timeInMillis))}, sys: ${sdf.format(Date(System.currentTimeMillis()))}"
        )
        var alarmMgr: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var intent = Intent(this, MainActivity::class.java)
        var pdIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        var timeInMillis = calander.timeInMillis
        if(!isLock() && System.currentTimeMillis() > calander.timeInMillis)
            timeInMillis += 24 * 3600000
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calander.timeInMillis, pdIntent)
    }

    private fun startTimer() {
        timer = Timer()
        timer.schedule(timerTask {
            tvTime.post {
                updateTime()
            }
        }, 0, 1000)
    }

    private fun updateTime() {
        var sdf = SimpleDateFormat("HH:mm:ss")
        tvTime.text = sdf.format(Date(System.currentTimeMillis()))
        if (isLock()) {
            timeLeft -= 1000
            var leftStr = sdf.format(Date(timeLeft - 8 * 3600000))
            tvTimeLeft.text = "剩余时间：$leftStr"
            receiver.updateTimeLeft(timeLeft)
        } else {
            btnExit.visibility = View.VISIBLE
            toggleTimeSetter(true)
            tvTimeLeft.text = "剩余时间：00:00:00"
            stopLockTask()
            btnExit.setOnClickListener {
                timer.cancel()
                finish()
            }
        }
    }

    private fun toggleTimeSetter(enable : Boolean){
        edtStartHour.isEnabled = enable
        edtStartMin.isEnabled = enable
        edtEndHour.isEnabled = enable
        edtEndMin.isEnabled = enable
    }

   fun isLock(): Boolean {
        var rst = true
        if (System.currentTimeMillis() < calander.timeInMillis) {
            rst = false
        } else if (System.currentTimeMillis() > calander.timeInMillis + timeLeft) {
            rst = false
        } else if (timeLeft <= 0) {
            rst = false
        }
        return rst
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.post {
                window.systemGestureExclusionRects =
                    listOf(Rect(0, 0, window.decorView.width, window.decorView.height))
            }
        }
        if (timeLeft <= 0)
            updateTime()
    }

    override fun finish() {
        timer?.cancel()
        super.finish()
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent?
    ): Boolean {
        Log.e(TAG, "keycode: $keyCode, event: ${event?.action}")
        if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK && isLock()) {
            Log.e(TAG, "home key is not worked")
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
