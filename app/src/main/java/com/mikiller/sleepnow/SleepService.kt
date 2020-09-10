package com.mikiller.sleepnow

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 *    author : junfeng.xia
 *    e-mail : junfeng.xia@smg.cn
 *    date   : 2020/9/10 11:32
 *    desc   :
 *    version: 1.0
 */
class SleepService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var intent =Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        var pdIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val ntfBuilder = NotificationCompat.Builder(this, packageName)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("sleep now")
            .setContentText("sleep now")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pdIntent, true)
        startForeground(0, ntfBuilder.build())
        startActivity(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }
}