package com.shaza.encryptdecryptvideos

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Job
import com.firebase.jobdispatcher.Trigger
import com.shaza.encryptdecryptvideos.Services.DecryptionServices
import com.shaza.encryptdecryptvideos.Services.EncryptionServices
import com.shaza.encryptdecryptvideos.Services.MyJobServices
import com.shaza.encryptdecryptvideos.Services.MyWorker
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private val fileNameEncrypt = "video encrypted"
        private val fileNameDecrypt = "video decrypted"
        private val key = "68mmjedPW9cIfwwN"
        private val specString = "oCmvxdqMc7Ven1p1"
    }

    private val storage_permission_code = 1
    lateinit var root: String
    lateinit var dispatcher: FirebaseJobDispatcher
    lateinit var job: Job

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        root = getExternalFilesDir("").toString()
//        runJobAtSpecificTime()
        runWorkerAtSpecificTime()
        // can pause and play only
//        videoView.setMediaController(MediaController(this, false))
        registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
        viewSwitcher.reset()
//        pause and play video when touch the video
        videoView.setOnTouchListener { _, _ ->
            if (videoView.isPlaying) {
                pause.visibility = VISIBLE
                viewSwitcher.showNext()
                pause.visibility = INVISIBLE
                play.visibility = INVISIBLE
                videoView.pause()
            } else {
                play.visibility = VISIBLE
                viewSwitcher.showPrevious()
                play.visibility = INVISIBLE
                pause.visibility = INVISIBLE
                videoView.start()
            }
            false
        }
    }

    fun download(view: View) {
        loading.visibility = VISIBLE
        checkPermission()
    }

    private fun checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),storage_permission_code)
            }else{
                startDownloading()
            }
        }else{
            startDownloading()
        }
    }

    private fun startDownloading() {
        val url =
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        val context = this.applicationContext

        //set notification when download completed
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.allowScanningByMediaScanner()
        request.setDestinationInExternalFilesDir(context,"", fileNameDecrypt)

        val manger = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        var downloadId = manger.enqueue(request)
//        checkDownloadStatus(downloadId, manger)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            storage_permission_code ->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    startDownloading()
                }else{
                    Toast.makeText(this,"download denied", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            loading.visibility = GONE
            downloadVideo.isEnabled = false
            val inputDecrypt = File(root, fileNameDecrypt)
            if (inputDecrypt.exists()) {
                playVideo()
                workerForEncryption()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val output = File(root, fileNameDecrypt)
        if (output.exists()) {
            output.delete()
        }

    }

    override fun onResume() {
        super.onResume()
        val output = File(root, fileNameDecrypt)
        val input = File(root, fileNameEncrypt)
        if (input.exists()) {
            if (output.exists()) {
                playVideo()
            } else {
                loading.visibility = VISIBLE
                workerForDecryption()
            }
        } else {
            downloadVideo.isEnabled = true
        }
    }

    //not used
    private fun runJobAtSpecificTime() {
        val currentTime = Calendar.getInstance()
        val currentTimeInSec = currentTime.timeInMillis / 1000
        val dateToClear = Calendar.getInstance()
//        run this job after one minute
        dateToClear.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE) + 1)
        val timeInSec = dateToClear.timeInMillis / 1000
        val reminderTime = timeInSec - currentTimeInSec
        Log.v("jobServices", dateToClear.toString())
        Log.v("jobServices", reminderTime.toString())
        dispatcher = FirebaseJobDispatcher(GooglePlayDriver(applicationContext))
        job = dispatcher.newJobBuilder()
            .setService(MyJobServices::class.java)
            .setTag("print message")
            .setRecurring(false)
            .setTrigger(Trigger.executionWindow(reminderTime.toInt(), reminderTime.toInt() + 5))
            .setReplaceCurrent(true)
            .build()

        dispatcher.mustSchedule(job)
    }

    private fun runWorkerAtSpecificTime() {
        val currentTime = Calendar.getInstance()
        val currentTimeInSec = currentTime.timeInMillis / 1000
        val dateToClear = Calendar.getInstance()
//        run this job after one minute
        dateToClear.set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE) + 1)
        val timeInSec = dateToClear.timeInMillis / 1000
        val reminderTime = timeInSec - currentTimeInSec
        Log.v("jobServices", dateToClear.toString())
        Log.v("jobServices", reminderTime.toString())
        val task = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setInitialDelay(reminderTime, TimeUnit.SECONDS).build()
        val workManger = WorkManager.getInstance(applicationContext)
        workManger.enqueue(task)
    }

    private fun workerForEncryption() {
        val data = Data.Builder()
        data.putString(AppConstants.rootFile, root)
        data.putString(AppConstants.inputFile, fileNameDecrypt)
        data.putString(AppConstants.outputFile, fileNameEncrypt)
        data.putString(AppConstants.key, key)
        data.putString(AppConstants.specString, specString)
        val task =
            OneTimeWorkRequest.Builder(EncryptionServices::class.java).setInputData(data.build())
                .build()
        val workManger = WorkManager.getInstance(applicationContext)
        workManger.enqueue(task)
    }

    private fun workerForDecryption() {
        val data = Data.Builder()
        data.putString(AppConstants.rootFile, root)
        data.putString(AppConstants.inputFile, fileNameEncrypt)
        data.putString(AppConstants.outputFile, fileNameDecrypt)
        data.putString(AppConstants.key, key)
        data.putString(AppConstants.specString, specString)
        val task =
            OneTimeWorkRequest.Builder(DecryptionServices::class.java).setInputData(data.build())
                .build()
        val workManger = WorkManager.getInstance(applicationContext)
        workManger.enqueue(task)
        workManger.getWorkInfoByIdLiveData(task.id).observe(this, androidx.lifecycle.Observer {
            if (it.state.isFinished) {
                Log.v("job", "finished")
                playVideo()
            }
        })
    }

    private fun playVideo() {
        val output = File(root, fileNameDecrypt)
        if (output.exists()) {
            loading.visibility = GONE
            videoView.setVideoURI(Uri.fromFile(output))
            videoView.requestFocus()
            videoView.start()
        }
    }
}
