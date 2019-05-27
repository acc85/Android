package org.helpapaw.helpapaw.sendsignal

import android.content.Context
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import org.helpapaw.helpapaw.R

/**
 * Created by iliyan on 0/29/16
 */
class SendSignalView : CardView {

    lateinit var imgSignalPhoto: ImageView
    lateinit var editSignalDescription: EditText
    lateinit var txtSignalSend: TextView
    lateinit var progressSendSignal: ProgressBar

    val signalDescription: String
        get() = editSignalDescription.text.toString().trim { it <= ' ' }

    constructor(context: Context) : super(context) {
        initViews(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initViews(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViews(context)
    }

    private fun initViews(context: Context) {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_send_signal, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        imgSignalPhoto = this.findViewById(R.id.img_signal_photo) as ImageView
        editSignalDescription = this.findViewById(R.id.edit_signal_description) as EditText
        txtSignalSend = this.findViewById(R.id.txt_signal_send) as TextView
        progressSendSignal = this.findViewById(R.id.progress_send_signal) as ProgressBar
    }

    fun setOnSignalSendClickListener(clickListener: View.OnClickListener) {
        txtSignalSend.setOnClickListener(clickListener)
    }

    fun setOnSignalPhotoClickListener(clickListener: View.OnClickListener) {
        imgSignalPhoto.setOnClickListener(clickListener)
    }

    fun setSignalPhoto(bitmap: RoundedBitmapDrawable) {
        imgSignalPhoto.scaleType = ImageView.ScaleType.FIT_XY
        imgSignalPhoto.setImageDrawable(bitmap)
    }

    fun clearData() {
        imgSignalPhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imgSignalPhoto.setImageResource(R.drawable.ic_camera)
        editSignalDescription.setText(null)
        setProgressVisibility(false)
    }

    fun setProgressVisibility(visibility: Boolean) {
        if (visibility) {
            progressSendSignal.visibility = View.VISIBLE
            txtSignalSend.visibility = View.GONE
        } else {
            txtSignalSend.visibility = View.VISIBLE
            progressSendSignal.visibility = View.GONE
        }
    }
}
