package org.helpapaw.helpapaw.sendsignal

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import kotlinx.android.synthetic.main.view_send_signal.view.*
import org.helpapaw.helpapaw.R

class SendSignalView : CardView {

    private lateinit var imgSignalPhoto: ImageView
    private lateinit var editSignalDescription: EditText
    private lateinit var txtSignalSend: TextView
    private lateinit var progressSendSignal: ProgressBar

    constructor(context: Context) : super(context) {
        initViews(context)
        initViews(context)
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
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
        imgSignalPhoto = img_signal_photo
        editSignalDescription = edit_signal_description
        txtSignalSend = txt_signal_send
        progressSendSignal = progress_send_signal
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

    fun getSignalDescription(): String {
        return editSignalDescription.text.toString().trim { it <= ' ' }
    }

    fun clearData() {
        imgSignalPhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imgSignalPhoto.setImageResource(R.drawable.ic_camera)
        editSignalDescription.text = null
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