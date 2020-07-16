package com.shaza.encryptdecryptvideos

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shaza.encryptdecryptvideos.Utils.MyEncrypter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*

class MainActivity : AppCompatActivity() {

    companion object{
        private val fileNameEncrypt = "video encrypted"
        private val fileNameDecrypt = "video decrypted"
        private val key = "68mmjedPW9cIfwwN"
        private val specString = "oCmvxdqMc7Ven1p1"
    }
    private val storage_permission_code = 1
    lateinit var root :String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        root = getExternalFilesDir("").toString()
        registerReceiver(onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
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
        val url = "http://techslides.com/demos/sample-videos/small.mp4"
        val request = DownloadManager.Request(Uri.parse(url))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        val context = this.applicationContext
        //Setting description of request
        request.setDescription("Your file is downloading")

        //set notification when download completed
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)

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
            loading.visibility = View.GONE
            val decryptInputFile: InputStream = FileInputStream("${root}/${fileNameDecrypt}")
            val encryptOutputFile : OutputStream = FileOutputStream("${root}/${fileNameEncrypt}")
            MyEncrypter.encryptToFile(key, specString,
                decryptInputFile,
                encryptOutputFile,
                "${root}/${fileNameDecrypt}"
            )
            val output = File(root, fileNameDecrypt)
            val decryptOutputFile: OutputStream = FileOutputStream(output)
            val encryptInputFile : InputStream = FileInputStream("${root}/${fileNameEncrypt}")
            MyEncrypter.decryptToFile(
                key, specString,
                encryptInputFile,decryptOutputFile
            )
            videoView.setVideoURI(Uri.fromFile(output))
            videoView.requestFocus()
            videoView.start()
            output.delete()
        }
    }
}
