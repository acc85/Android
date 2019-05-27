package org.helpapaw.helpapaw.sendsignal

import android.app.Dialog
import android.os.Bundle
import android.support.annotation.IntDef
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View
import android.widget.LinearLayout

import org.helpapaw.helpapaw.R

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by iliyan on 7/31/16
 */
class SendPhotoBottomSheet : BottomSheetDialogFragment() {
    private var behavior: BottomSheetBehavior<*>? = null

    internal var listener: PhotoTypeSelectListener? = null

    @IntDef(PhotoType.CAMERA.toLong(), PhotoType.GALLERY.toLong())
    @Retention(RetentionPolicy.SOURCE)
    annotation class PhotoType {
        companion object {
            const val CAMERA = 1
            const val GALLERY = 2
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.bottom_sheet_send_photo, null)

        val grpCameraOption = view.findViewById(R.id.grp_camera_option) as LinearLayout
        val grpGalleryOption = view.findViewById(R.id.grp_gallery_option) as LinearLayout

        dialog.setContentView(view)
        behavior = BottomSheetBehavior.from(view.parent as View)

        if (listener != null) {
            grpCameraOption.setOnClickListener {
                behavior!!.state = BottomSheetBehavior.STATE_HIDDEN
                listener!!.onPhotoTypeSelected(PhotoType.CAMERA)
            }

            grpGalleryOption.setOnClickListener {
                behavior!!.state = BottomSheetBehavior.STATE_HIDDEN
                listener!!.onPhotoTypeSelected(PhotoType.GALLERY)
            }
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun setListener(listener: PhotoTypeSelectListener) {
        this.listener = listener
    }

    interface PhotoTypeSelectListener {
        fun onPhotoTypeSelected(@PhotoType photoType: Int)
    }

    companion object {

        val TAG = SendPhotoBottomSheet::class.java.simpleName
    }
}
