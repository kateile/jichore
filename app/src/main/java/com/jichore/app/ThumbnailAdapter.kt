package com.jichore.app

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.devs.sketchimage.SketchImage

class ThumbnailAdapter(
    private val mContext: Context,
    private val sketchImage: SketchImage,
    private val bmOriginal: Bitmap,
    private val effectsList: List<Int>,
    private val thumbnailCallback: ThumbnailCallback
) : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.z_image, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return effectsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(effectsList[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.imageView)

        fun bind(effectType: Int) {
            imageView?.setImageBitmap(bmOriginal)

            imageView?.setImageBitmap(
                sketchImage.getImageAs(effectType, 90)
            )

            imageView.setOnClickListener { thumbnailCallback.onThumbnailClick(effectType) }
        }
    }

}