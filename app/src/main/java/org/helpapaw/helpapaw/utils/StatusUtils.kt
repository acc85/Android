package org.helpapaw.helpapaw.utils

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.PawApplication
import org.helpapaw.helpapaw.models.Signal

/**
 * Created by milen on 29/08/17.
 */

object StatusUtils {

    fun getStatusStringForCode(statusCode: Int): String {
        when (statusCode) {
            Signal.SOLVED -> return PawApplication.getContext().getString(R.string.txt_solved)
            Signal.SOMEBODY_ON_THE_WAY -> return PawApplication.getContext().getString(R.string.txt_somebody_on_the_way)
            else -> return PawApplication.getContext().getString(R.string.txt_help_needed)
        }
    }

    fun getPinResourceForCode(statusCode: Int): Int {
        when (statusCode) {
            Signal.SOLVED -> return R.drawable.pin_green
            Signal.SOMEBODY_ON_THE_WAY -> return R.drawable.pin_orange
            else -> return R.drawable.pin_red
        }
    }
}