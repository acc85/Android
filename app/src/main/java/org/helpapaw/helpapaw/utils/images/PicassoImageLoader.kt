package org.helpapaw.helpapaw.utils.images

import android.content.Context
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.squareup.picasso.Picasso

class PicassoImageLoader : ImageLoader{

    override fun load(context: Context?, url: String, imageView: ImageView, @DrawableRes placeholder: Int) {
        Picasso.with(context)
                .load(url)
                .error(placeholder)
                .fit()
                .centerInside()
                .into(imageView)
    }

    override fun loadWithRoundedCorners(context: Context?, url: String, imageView: ImageView, @DrawableRes placeholder: Int) {
        Picasso.with(context)
                .load(url)
                .placeholder(placeholder)
                .transform(RoundedTransformation(16, 0))
                .fit()
                .centerCrop()
                .into(imageView)
    }

}