package com.jichore.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_futa.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class FutaActivity : AppCompatActivity(), CoroutineScope {
    lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_futa)

        job = Job()

        textView.text = getString(R.string.app_name)
        getThings()

    }

    private fun getThings() = launch {
        delay(2000)
        Log.d("where", "Thread is ${Thread.currentThread().name}")
        textView.text = getString(R.string.share_app)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
