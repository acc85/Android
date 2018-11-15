package org.helpapaw.helpapaw.sendsignal

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IntDef
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.helpapaw.helpapaw.R

class SendPhotoBottomSheet : BottomSheetDialogFragment() {


    private var behavior: BottomSheetBehavior<*>? = null

    companion object {
        val TAG = SendPhotoBottomSheet::class.simpleName

        @IntDef(CAMERA, GALLERY)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        annotation class PhotoType

        const val CAMERA = 1
        const val GALLERY = 2
    }

    var listener: PhotoTypeSelectListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.bottom_sheet_send_photo, null)

        val grpCameraOption = view.findViewById<View>(R.id.grp_camera_option) as LinearLayout
        val grpGalleryOption = view.findViewById<View>(R.id.grp_gallery_option) as LinearLayout

        dialog.setContentView(view)
        behavior = BottomSheetBehavior.from(view.parent as View)

        if (listener != null) {
            grpCameraOption.setOnClickListener {
                behavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
                listener?.onPhotoTypeSelected(CAMERA)
            }

            grpGalleryOption.setOnClickListener {
                behavior?.setState(BottomSheetBehavior.STATE_HIDDEN)
                listener?.onPhotoTypeSelected(GALLERY)
            }
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        behavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
    }

    interface PhotoTypeSelectListener {
        fun onPhotoTypeSelected(@PhotoType photoType: Int)
    }

}