package com.tails.presentation.streaming.service

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters

class ChooseServiceType(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    override fun doWork(): Result {
        //applicationContext.stopService(Intent(applicationContext, ObserveTaskRemovedService::class.java))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(Intent(applicationContext, ObserveTaskRemovedService::class.java))
        } else {
            applicationContext.startService(Intent(applicationContext, ObserveTaskRemovedService::class.java))
        }
        return Result.success()
    }
}