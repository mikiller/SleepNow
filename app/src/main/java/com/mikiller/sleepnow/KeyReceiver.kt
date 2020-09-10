package com.mikiller.sleepnow

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

/**
 *    author : junfeng.xia
 *    e-mail : junfeng.xia@smg.cn
 *    date   : 2020/9/9 17:42
 *    desc   :
 *    version: 1.0
 */
class KeyReceiver(var act:MainActivity, private var timeLeft:Long = 0, private var startTime:Long) : BroadcastReceiver() {
    private val TAG = KeyReceiver::class.java.simpleName
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        var action = intent?.action;
        if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS == action) {
            var reason = intent?.getStringExtra("reason")
            var currentTime = System.currentTimeMillis()
            if(!act.isLock())
                return;
            if(timeLeft <= 0){
                Log.e(TAG, "stop lock task")
                act.stopLockTask()
                return;
            }
            reason?.let {
                when (it) {
                    "homekey" -> {
                        context?.startActivity(Intent(context, MainActivity::class.java))
                    }
                    "recentapps" -> {
                        Log.e(TAG, "click task")
                        context?.startActivity(Intent(context, MainActivity::class.java))
                        var alarmMgr : AlarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        var intent = Intent(context!!, MainActivity::class.java)
                        var pdIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, 20, pdIntent)

                    }
                    else -> {
                        Log.e(TAG, "reason: $it")
                    }
                }
            }
        }
    }

    fun updateTimeLeft(left : Long) {
        timeLeft = left
    }
}