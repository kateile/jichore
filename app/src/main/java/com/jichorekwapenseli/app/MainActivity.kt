package com.jichorekwapenseli.app

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devs.sketchimage.SketchImage
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import com.google.android.material.tabs.TabLayout
import com.himanshurawat.imageworker.Extension
import com.himanshurawat.imageworker.ImageWorker
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var bmOriginal: Bitmap
    private lateinit var sketchImage: SketchImage
    private val maxProgress = 100
    private var effectType = SketchImage.ORIGINAL_TO_GRAY
    private val requestCode = 400
    private lateinit var imageUri: Uri
    private var newImageUrl: Uri? = null

    private enum class CONDITION {
        NOTHING,
        EDITING,
        SAVED,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        render(CONDITION.NOTHING)
        fabAdd.setOnClickListener { Pix.start(this, requestCode) }
        fabSave.setOnClickListener { saveImage() }
    }

    private fun render(c: CONDITION) {
        when (c) {
            CONDITION.NOTHING -> {
                container.visibility = View.INVISIBLE
                fabAdd.visibility = View.VISIBLE
            }
            CONDITION.EDITING -> {
                container.visibility = View.VISIBLE
                fabAdd.visibility = View.VISIBLE
                fabSave.visibility = View.VISIBLE
                fabShare.visibility = View.GONE
            }
            CONDITION.SAVED -> {
                fabAdd.visibility = View.VISIBLE
                fabSave.visibility = View.GONE
                fabShare.visibility = View.VISIBLE
            }
        }
    }

    private fun saveImage() {
        val bitmap = sketchImage.getImageAs(
            effectType,
            maxProgress
        )

        ImageWorker
            .to(this)
            .directory("Penseli")
            .setFileName(imageUri.pathSegments.last())
            .withExtension(Extension.PNG)
            .save(bitmap, 100)

        render(CONDITION.SAVED)
        toast("Picture has been saved to Penseli folder.")
    }

    private fun toast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun showImage() {
        val bmOriginal = BitmapFactory.decodeFile(imageUri.path)

        targetImageView?.setImageBitmap(bmOriginal)

        sketchImage = SketchImage.Builder(this, bmOriginal).build()

        percentTextView.text = String.format("%d %%", maxProgress)
        seekBar.max = maxProgress
        seekBar.progress = maxProgress
        targetImageView?.setImageBitmap(
            sketchImage.getImageAs(
                effectType,
                maxProgress
            )
        )

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Gray"))
        tabLayout.addTab(tabLayout.newTab().setText("Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Colored Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Soft Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Soft Color Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Gray to Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Gray to Colored Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Gray to Soft Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Gray to Soft Color Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Sketch to Color Sketch"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                effectType = tabLayout.selectedTabPosition
                percentTextView.text = String.format("%d %%", maxProgress)
                seekBar.max = maxProgress
                seekBar.progress = maxProgress
                targetImageView?.setImageBitmap(
                    sketchImage.getImageAs(
                        effectType,
                        maxProgress
                    )
                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                percentTextView.text = String.format("%d %%", seekBar.progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                progressBar.visibility = View.INVISIBLE
                targetImageView?.setImageBitmap(
                    sketchImage.getImageAs(
                        effectType,
                        seekBar.progress
                    )
                )
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == this.requestCode) {
            val returnValue = data!!.getStringArrayListExtra(Pix.IMAGE_RESULTS)

            imageUri = Uri.parse(returnValue[0])
            render(CONDITION.EDITING)
            showImage()
        } else toast("No image added")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(this, requestCode)
                } else {
                    Toast.makeText(this@MainActivity, "Approve permissions to open image picker", Toast.LENGTH_LONG)
                        .show()
                }
                return
            }
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
}
