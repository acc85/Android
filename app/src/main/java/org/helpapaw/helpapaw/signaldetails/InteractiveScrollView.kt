package org.helpapaw.helpapaw.signaldetails

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.ScrollView

class InteractiveScrollView : ScrollView{

    var listener: OnBottomReachedListener? = null
    private var isAtTheBottom: Boolean = false

    constructor(context:Context): super(context){
        isAtTheBottom  =false
    }

    constructor(context:Context, attrs: AttributeSet): super(context,attrs){
        isAtTheBottom  =false
    }


    constructor(context:Context, attrs: AttributeSet, defStyle:Int ): super(context,attrs,defStyle){
        isAtTheBottom  =false
    }


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        val view = getChildAt(childCount - 1)
        val diff = view.bottom - (height + scrollY)

        if (listener != null) {
            if (diff == 0 && !isAtTheBottom) {
                listener?.onBottomReached(true)
                isAtTheBottom = true
            } else if (diff > getPixels(TypedValue.COMPLEX_UNIT_DIP, 10f) && isAtTheBottom) {
                listener?.onBottomReached(false)
                isAtTheBottom = false
            }
        }

        super.onScrollChanged(l, t, oldl, oldt)
    }

    fun setOnBottomReachedListener(onBottomReachedListener: OnBottomReachedListener) {
        listener = onBottomReachedListener
    }


    fun getPixels(unit: Int, size: Float): Int {
        val metrics = Resources.getSystem().displayMetrics
        return TypedValue.applyDimension(unit, size, metrics).toInt()
    }


    fun canScroll(): Boolean {
        val child = getChildAt(0)
        if (child != null) {
            val childHeight = child.height
            return height < childHeight
        }
        return false
    }

    interface OnBottomReachedListener {
        fun onBottomReached(isBottomReached: Boolean)
    }
}