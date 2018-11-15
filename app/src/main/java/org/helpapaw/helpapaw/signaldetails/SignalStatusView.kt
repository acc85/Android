package org.helpapaw.helpapaw.signaldetails

import android.content.Context
import android.content.res.Resources
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import kotlinx.android.synthetic.main.view_signal_status.view.*
import org.helpapaw.helpapaw.R
import java.util.*

class SignalStatusView : FrameLayout, SignalStatusViewContract {


    private lateinit var grpStatusContainer: FrameLayout

    private lateinit var grpSignalNeedHelp: LinearLayout
    private lateinit var grpSignalOnWay: LinearLayout
    private lateinit var grpSignalSolved: LinearLayout
    private lateinit var grpProgressBar: ProgressBar

    private lateinit var imgNeedHelpSelected: AppCompatImageView
    private lateinit var imgOnWaySelected: AppCompatImageView
    private lateinit var imgSolvedSelected: AppCompatImageView

    private var selectedStatus: Int = 0
    private var isExpanded = false
    private var callback: StatusCallback? = null
    private val statusList = ArrayList<LinearLayout>()
    private val selectedImages = ArrayList<AppCompatImageView>()

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
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_signal_status, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initUi()
        initData()
        setStatusClickListeners(statusList)
    }

    private fun initUi() {
        grpStatusContainer = grp_status_container

        grpSignalNeedHelp = grp_signal_need_help
        grpSignalOnWay = grp_signal_on_way
        grpSignalSolved = grp_signal_solved

        grpProgressBar = grp_progress_bar

        imgNeedHelpSelected = img_signal_need_help_selected
        imgOnWaySelected = grpSignalOnWay.img_signal_on_way_selected
        imgSolvedSelected = grpSignalSolved.img_signal_solved_selected
    }

    private fun initData() {
        statusList.add(grpSignalNeedHelp)
        statusList.add(grpSignalOnWay)
        statusList.add(grpSignalSolved)

        selectedImages.add(imgNeedHelpSelected)
        selectedImages.add(imgOnWaySelected)
        selectedImages.add(imgSolvedSelected)
    }

    private fun expandStatusView(status: Int) {
        for (i in statusList.indices) {
            statusList[i].isClickable = false
            statusList[i].visibility = View.VISIBLE
            val lastStatusId = statusList.size - 1
            if (i != lastStatusId) {
                statusList[i].animate().translationYBy(getPixels(TypedValue.COMPLEX_UNIT_DIP, (i * 80).toFloat()).toFloat()).alpha(1f)
            } else {
                statusList[i].animate().translationYBy(getPixels(TypedValue.COMPLEX_UNIT_DIP, (i * 80).toFloat()).toFloat()).alpha(1f).withEndAction {
                    for (index in statusList.indices) {
                        statusList[index].isClickable = true
                    }
                }
            }

            if (i != status) {
                selectedImages[i].setImageResource(0)
            }
        }

        animateStatusViewHeight(getPixels(TypedValue.COMPLEX_UNIT_DIP, (3 * 80).toFloat()))
        selectedImages[status].setImageResource(R.drawable.ic_done)
    }

    private fun collapseStatusView(status: Int) {

        for (i in statusList.indices) {
            if (i != status) {
                statusList[i].animate().alpha(0f)
            }
        }

        grpSignalOnWay.animate().translationYBy((-getPixels(TypedValue.COMPLEX_UNIT_DIP, 80f)).toFloat())
        grpSignalSolved.animate().translationYBy((-getPixels(TypedValue.COMPLEX_UNIT_DIP, (2 * 80).toFloat())).toFloat()).withEndAction { updateStatus(status) }
        animateStatusViewHeight(getPixels(TypedValue.COMPLEX_UNIT_DIP, 80f))
    }

    fun updateStatus(status: Int) {
        for (i in statusList.indices) {
            if (i != status) {
                statusList[i].visibility = View.GONE
            } else {
                statusList[i].visibility = View.VISIBLE
            }
        }
        this.selectedStatus = status
        selectedImages[status].setImageResource(R.drawable.ic_dropdown)
    }

    private fun animateStatusViewHeight(newHeight: Int) {
        val resizeAnimation = ResizeAnimation(
                grpStatusContainer,
                grpStatusContainer.height.toFloat(),
                newHeight.toFloat()
        )
        resizeAnimation.duration = 300
        grpStatusContainer.startAnimation(resizeAnimation)
    }

    private fun isExpanded(): Boolean {
        return !(grpSignalSolved.visibility != View.VISIBLE ||
                grpSignalOnWay.visibility != View.VISIBLE ||
                grpSignalNeedHelp.visibility != View.VISIBLE)
    }

    fun getPixels(unit: Int, size: Float): Int {
        val metrics = Resources.getSystem().displayMetrics
        return TypedValue.applyDimension(unit, size, metrics).toInt()
    }

    fun setStatusCallback(callback: StatusCallback) {
        this.callback = callback
    }

    fun getSelectedStatus(): Int {
        return this.selectedStatus
    }

    fun setStatusClickListeners(statusList: List<LinearLayout>) {
        for (i in statusList.indices) {
            statusList[i].setOnClickListener {
                if (isExpanded()) {
                    if (getSelectedStatus() != i) {
                        if (callback != null) {
                            grpProgressBar.visibility = View.VISIBLE

                            callback?.onRequestStatusChange(i)
                        }
                    }
                } else {
                    expandStatusView(i)
                }
            }
        }
    }

    override fun onStatusChangeRequestFinished(success: Boolean, newStatus: Int) {

        grpProgressBar.visibility = View.GONE

        if (success) {
            if (isExpanded()) {
                collapseStatusView(newStatus)
            } else {
                expandStatusView(newStatus)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        val savedState = SavedState(superState)

        savedState.selectedStatus = this.selectedStatus
        savedState.isExpanded = isExpanded()

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        val savedState = state
        super.onRestoreInstanceState(savedState.getSuperState())

        this.selectedStatus = savedState.selectedStatus
        this.isExpanded = savedState.isExpanded

        restoreState(selectedStatus, isExpanded)
    }

    private fun restoreState(selectedStatus: Int, isExpanded: Boolean) {
        if (isExpanded) {
            expandStatusView(selectedStatus)
        } else {
            updateStatus(selectedStatus)
        }
    }

    internal class SavedState : BaseSavedState {

        var selectedStatus: Int = 0

        var isExpanded: Boolean = false

        constructor(parcel: Parcel) : super(parcel) {
            selectedStatus = parcel.readInt()
            isExpanded = parcel.readByte().toInt() != 0
        }


        constructor(parcelable: Parcelable?) : super(parcelable)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(this.selectedStatus)
            out.writeByte((if (isExpanded) 1 else 0).toByte())
        }


        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}

internal class ResizeAnimation(private val view: View, private val fromHeight: Float, private val toHeight: Float) : Animation() {

    init {
        duration = 300
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val height = (toHeight - fromHeight) * interpolatedTime + fromHeight
        val p = view.layoutParams
        p.height = height.toInt()
        view.requestLayout()
    }
}

interface StatusCallback {
    fun onRequestStatusChange(status: Int)
}

interface SignalStatusViewContract {
    fun onStatusChangeRequestFinished(success: Boolean, newStatus: Int)
}