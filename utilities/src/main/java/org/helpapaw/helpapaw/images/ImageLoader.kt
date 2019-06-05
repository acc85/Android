package org.helpapaw.helpapaw.images

import androidx.annotation.DrawableRes
import android.widget.ImageView

/**
 * Created by iliyan on 7/29/16
 */
interface ImageLoader {

    fun load(url: String, imageView: ImageView, @DrawableRes placeholder: Int)

    fun loadWithRoundedCorners(url: String, imageView: ImageView, @DrawableRes placeholder: Int)

}
