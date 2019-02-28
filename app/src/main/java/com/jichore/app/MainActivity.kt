package com.jichore.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.devs.sketchimage.SketchImage
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.himanshurawat.imageworker.Extension
import com.himanshurawat.imageworker.ImageWorker
import com.jichore.app.Consts.APP_FOLDER
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.editor.*
import kotlinx.android.synthetic.main.placeholder.*
import kotlinx.coroutines.*
import org.jetbrains.anko.design.longSnackbar
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), ThumbnailCallback, CoroutineScope {

    private lateinit var sketchImage: SketchImage
    private val maxProgress = 100
    private var effectType = SketchImage.ORIGINAL_TO_SKETCH
    private lateinit var imageUri: Uri
    private var savedImageUrl: Uri? = null
    private lateinit var bmOriginal: Bitmap
    private var images = ArrayList<Image>()

    private lateinit var interstitialAd: InterstitialAd

    private enum class CONDITION {
        NOTHING,
        EDITING,
        SAVED,
    }

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        job = Job()

        loadAds()

        render(CONDITION.NOTHING)
        fabAdd.setOnClickListener { openGallery() }
        fabSave.setOnClickListener { saveImage() }
        fabShare.setOnClickListener { onShare() }
    }

    private fun openGallery() {
        ImagePicker.with(this@MainActivity)           //  Initialize ImagePicker with activity or fragment context
            .setToolbarColor("#212121")         //  Toolbar color
            .setStatusBarColor("#000000")       //  StatusBar color (works with SDK >= 21  )
            .setToolbarTextColor("#FFFFFF")     //  Toolbar text color (Title and Done button)
            .setToolbarIconColor("#FFFFFF")     //  Toolbar icon color (Back and Camera button)
            .setProgressBarColor("#4CAF50")     //  ProgressBar color
            .setBackgroundColor("#212121")      //  Background color
            .setCameraOnly(false)               //  Camera mode
            .setMultipleMode(false)              //  Select multiple images or single image
            .setFolderMode(true)                //  Folder mode
            .setShowCamera(true)                //  Show camera button
            .setFolderTitle("Albums")           //  Folder title (works with FolderMode = true)
            .setImageTitle("Galleries")         //  Image title (works with FolderMode = false)
            .setSavePath(Environment.DIRECTORY_PICTURES)         //  Image capture folder name
            .setSelectedImages(images)          //  Selected images
            .setRequestCode(100)                //  Set request code, default Config.RC_PICK_IMAGES
            .setKeepScreenOn(true)              //  Keep screen on when selecting images
            .start()                          //  Start ImagePicker
    }

    private fun processImage() = launch {
        showProgress(true)

        showImage()
        initThumbnailsList()
        onSeekBarChange()
    }

    private fun showImage() = launch {
        bmOriginal = BitmapFactory.decodeFile(imageUri.path)

        targetImageView?.setImageBitmap(bmOriginal)

        percentTextView.text = String.format("%d %%", getProgress(this@MainActivity, effectType))
        seekBar.max = maxProgress
        seekBar.progress = getProgress(this@MainActivity, effectType)

        targetImageView?.setImageBitmap(doImage(effectType, getProgress(this@MainActivity, effectType)))
    }

    private suspend fun doImage(effectType: Int, percent: Int): Bitmap {
        showProgress(true)

        val newImage = withContext(Dispatchers.Default) {
            sketchImage = SketchImage.Builder(this@MainActivity, bmOriginal).build()

            sketchImage.getImageAs(effectType, percent)
        }
        showProgress(false)

        return newImage
    }

    private fun initThumbnailsList() = launch {
        showProgress(true)

        val layoutManager = LinearLayoutManager(this@MainActivity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        layoutManager.scrollToPosition(0)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        val adapter = withContext(Dispatchers.Default) {
            ThumbnailAdapter(
                this@MainActivity, sketchImage, bmOriginal, Thumbnails().filters, this@MainActivity
            )
        }
        recyclerView.adapter = adapter
        showProgress(false)
    }

    override fun onThumbnailClick(effect: Int) = launch {
        effectType = effect
        percentTextView.text = String.format("%d %%", getProgress(this@MainActivity, effectType))
        seekBar.max = maxProgress
        seekBar.progress = getProgress(this@MainActivity, effectType)

        targetImageView?.setImageBitmap(doImage(effectType, getProgress(this@MainActivity, effectType)))
    }

    private fun onSeekBarChange() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                percentTextView.text = String.format("%d %%", seekBar.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                showProgress(true)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Saving current value.
                saveProgress(this@MainActivity, effectType, seekBar.progress)

                launch {
                    targetImageView?.setImageBitmap(doImage(effectType, seekBar.progress))
                }
            }
        })
    }

    private fun render(c: CONDITION) {
        when (c) {
            CONDITION.NOTHING -> {
                placeholder.visibility = View.VISIBLE
                container.visibility = View.GONE
                fabAdd.visibility = View.VISIBLE
            }
            CONDITION.EDITING -> {
                placeholder.visibility = View.GONE
                container.visibility = View.VISIBLE
                fabAdd.visibility = View.VISIBLE
                fabSave.visibility = View.VISIBLE
                fabShare.visibility = View.GONE
            }
            CONDITION.SAVED -> {
                fabAdd.visibility = View.VISIBLE
                fabSave.visibility = View.VISIBLE
                fabShare.visibility = View.VISIBLE
            }
        }
    }

    private fun saveImage() = launch {
        showProgress(true)

        val bitmap = sketchImage.getImageAs(
            effectType,
            maxProgress
        )

        val name = getRandomString(10)
        //val folder = Environment.getExternalStorageDirectory()

        ImageWorker
            .to(this@MainActivity)
            .directory(Environment.DIRECTORY_PICTURES)
            .subDirectory(APP_FOLDER)
            .setFileName(name)
            .withExtension(Extension.PNG)
            .save(bitmap, 100)

        render(CONDITION.SAVED)
        //Showing success message.
        val snackBar =
            coordinatorLayout.longSnackbar(getString(R.string.photo_saved), getString(R.string.share)) { onShare() }

        snackBar.addCallback(object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                showProgress(false)
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                //if (interstitialAd.isLoaded) interstitialAd.show()
            }
        })

        savedImageUrl = Uri.parse("${Environment.DIRECTORY_PICTURES}/$APP_FOLDER/$name.png")
        Log.d("image", savedImageUrl.toString())
    }

    private fun onShare() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"

        intent.putExtra(Intent.EXTRA_STREAM, savedImageUrl)
        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun toast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == Activity.RESULT_OK && data != null) {
            val images = data.getParcelableArrayListExtra<Image>(Config.EXTRA_IMAGES)
            imageUri = Uri.parse(images[0].path)
            render(CONDITION.EDITING)
            processImage()
        } else toast("No image added")
        // You MUST have this line to be here // so ImagePicker can work with fragment mode
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showProgress(boolean: Boolean) {
        when (boolean) {
            true -> progressBar.visibility = View.VISIBLE
            false -> progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadAds() {
        MobileAds.initialize(this, getString(R.string.admob_app_id))

        adView.loadAd(AdRequest.Builder().build())
        adViewBanner.loadAd(AdRequest.Builder().build())

        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = getString(R.string.interstitial_ad_unit)
        interstitialAd.loadAd(AdRequest.Builder().build())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (interstitialAd.isLoaded) interstitialAd.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel job on activity destroy. After destroy all children jobs will be cancelled automatically
    }

}
