package org.helpapaw.helpapaw.utils.images

import android.content.Context
import android.widget.ImageView
import androidx.annotation.DrawableRes

interface ImageLoader {

    fun load(context: Context?, url: String, imageView: ImageView, @DrawableRes placeholder: Int)

    fun loadWithRoundedCorners(context: Context?, url: String, imageView: ImageView, @DrawableRes placeholder: Int)

}