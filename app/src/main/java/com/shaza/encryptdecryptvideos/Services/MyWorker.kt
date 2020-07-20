package com.shaza.encryptdecryptvideos.Services

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters


class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.v("job", "deleted file")
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable { // Run your task here
            Toast.makeText(applicationContext, "deleted file", Toast.LENGTH_LONG).show()
        }, 1000)
        return Result.success()
    }
}