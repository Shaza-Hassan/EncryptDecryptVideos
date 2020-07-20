package com.shaza.encryptdecryptvideos.Services

import android.util.Log
import android.widget.Toast
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService

class MyJobServices : JobService() {
    override fun onStopJob(job: JobParameters): Boolean {
        return false
    }

    override fun onStartJob(job: JobParameters): Boolean {
        Log.v("jobServices", "Job started")
        Toast.makeText(applicationContext, "Job started", Toast.LENGTH_LONG).show()
        return false
    }
}