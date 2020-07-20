package com.shaza.encryptdecryptvideos.Services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.shaza.encryptdecryptvideos.AppConstants
import com.shaza.encryptdecryptvideos.Utils.MyEncrypter
import java.io.*

class DecryptionServices(context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {
        val root = inputData.getString(AppConstants.rootFile)
        val inputFileName = inputData.getString(AppConstants.inputFile)
        val outputFileName = inputData.getString(AppConstants.outputFile)
        val key = inputData.getString(AppConstants.key)
        val specString = inputData.getString(AppConstants.specString)
        val inputEncrypt = File(root, inputFileName)
        val outputDecrypt = File(root, outputFileName)
        val decryptInputFile: InputStream = FileInputStream(inputEncrypt)
        val decryptOutputFile: OutputStream = FileOutputStream(outputDecrypt)
        if (key != null && specString != null) {
            MyEncrypter.decryptToFile(
                key, specString,
                decryptInputFile,
                decryptOutputFile
            )
        }
        return Result.success()
    }
}