package org.helpapaw.helpapaw.images;

import androidx.annotation.DrawableRes;
import android.widget.ImageView;

/**
 * Created by iliyan on 7/29/16
 */
public interface ImageLoader {

    void load(String url, ImageView imageView, @DrawableRes int placeholder);

    void loadWithRoundedCorners(String url, ImageView imageView, @DrawableRes int placeholder);

}
