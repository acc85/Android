package org.helpapaw.helpapaw.sendsignal

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.LongDef
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import org.helpapaw.helpapaw.R

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by iliyan on 7/31/16
 */
class SendPhotoBottomSheet : BottomSheetDialogFragment() {
    private var behavior: BottomSheetBehavior<*>? = null

    internal var listener: PhotoTypeSelectListener? = null

    @LongDef(PhotoType.CAMERA, PhotoType.GALLERY)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class PhotoType {
        companion object {
            const val CAMERA:Long = 1
            const val GALLERY:Long = 2
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
        fun onPhotoTypeSelected(@PhotoType photoType: Long)
    }

    companion object {

        val TAG = SendPhotoBottomSheet::class.java.simpleName
    }
}
