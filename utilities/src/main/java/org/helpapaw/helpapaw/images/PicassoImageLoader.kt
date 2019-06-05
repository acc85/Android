package org.helpapaw.helpapaw.images

import androidx.annotation.DrawableRes
import android.widget.ImageView

import com.squareup.picasso.Picasso

/**
 * Created by iliyan on 8/2/16
 */
class PicassoImageLoader : ImageLoader {

    override fun load(url: String, imageView: ImageView, @DrawableRes error: Int) {
        Picasso.get()
                .load(url)
                .error(error)
                .fit()
                .centerInside()
                .into(imageView)
    }

    override fun loadWithRoundedCorners(url: String, imageView: ImageView, @DrawableRes placeholder: Int) {
        Picasso.get()
                .load(url)
                .placeholder(placeholder)
                .transform(RoundedTransformation(16, 0))
                .fit()
                .centerCrop()
                .into(imageView)
    }

}