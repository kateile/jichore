package com.jichore.app

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.devs.sketchimage.SketchImage


class TheWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val applicationContext = applicationContext
        Log.d(TAG, "Performing long running task in scheduled job")

        return try {
            val bmOriginal = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.test)
            val sketchImage = SketchImage.Builder(applicationContext, bmOriginal).build()

            val bmProcessed = sketchImage.getImageAs(
                SketchImage.ORIGINAL_TO_SKETCH, 100
            )
            imageView.setImageBitmap(bmProcessed)

            Result.success()

        } catch (t: Throwable) {
            Log.e(TAG, "Error", t)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "TheWorker"
    }
}