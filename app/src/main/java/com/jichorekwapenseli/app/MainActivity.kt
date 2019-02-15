package com.jichorekwapenseli.app

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.devs.sketchimage.SketchImage
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var bmOriginal: Bitmap
    private lateinit var sketchImage: SketchImage
    private val MAX_PROGRESS = 100
    private var effectType = SketchImage.ORIGINAL_TO_GRAY


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val bmOriginal = BitmapFactory.decodeResource(resources, R.drawable.test)
        //Bitmap bmOriginal = decodeSampledBitmapFromResource(getResources(), R.drawable.usr, 100, 100);

        targetImageView?.setImageBitmap(bmOriginal)

        sketchImage = SketchImage.Builder(this, bmOriginal).build()

        percentTextView.text = String.format("%d %%", MAX_PROGRESS)
        seekBar.max = MAX_PROGRESS
        seekBar.progress = MAX_PROGRESS
        targetImageView?.setImageBitmap(
            sketchImage.getImageAs(
                effectType,
                MAX_PROGRESS
            )
        )

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Original to Gray"))
        tabLayout.addTab(tabLayout.newTab().setText("Original to Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Original to Colored Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Original to Soft Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Original to Soft Color Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Gray to Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Gray to Colored Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Gray to Soft Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Gray to Soft Color Sketch"))
        tabLayout.addTab(tabLayout.newTab().setText("Sketch to Color Sketch"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                effectType = tabLayout.selectedTabPosition
                percentTextView.text = String.format("%d %%", MAX_PROGRESS)
                seekBar.max = MAX_PROGRESS
                seekBar.progress = MAX_PROGRESS
                targetImageView?.setImageBitmap(
                    sketchImage.getImageAs(
                        effectType,
                        MAX_PROGRESS
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

    private fun decodeSampledBitmapFromResource(res: Resources, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(res, resId, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(res, resId, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
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
