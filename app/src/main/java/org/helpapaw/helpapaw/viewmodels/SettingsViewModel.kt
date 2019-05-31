package org.helpapaw.helpapaw.viewmodels

import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import org.helpapaw.helpapaw.BR
import org.helpapaw.helpapaw.repository.ISettingsRepository
import java.util.*

class SettingsViewModel(
        val toolbarText: String,
        val radiusOutputSingleText: String,
        val radiusOutputText: String,
        val timeoutOutputSingleText: String,
        val timeoutOutputText: String,
        val settingsRepository: ISettingsRepository
) : BaseViewModel() {

    @Bindable
    var toolbarTitle: String = toolbarText

    @Bindable
    var radiusProgress: Int = 0
        set(value) {
            field = value
            updateRadius(field)
            notifyChange(BR.radiusProgress)
        }


    @Bindable
    var timeoutProgress: Int = 0
        set(value) {
            field = value
            updateTimeout(field)
            notifyChange(BR.timeoutProgress)
        }

    @Bindable
    var radiusText: String = "0 kilometers"
        set(value) {
            field = value
            notifyChange(BR.radiusText)
        }

    @Bindable
    var timeoutText: String = ""
        set(value) {
            field = value
            notifyChange(BR.timeoutText)
        }


    fun saveRadiusProgress() {
        settingsRepository.saveRadius(radiusProgress)
    }

    fun saveTimeOutProgress() {
        settingsRepository.saveTimeout(timeoutProgress)
    }


    fun setRadius() {
        radiusProgress = settingsRepository.getRadius()
    }

    fun setTimeout() {
        timeoutProgress = settingsRepository.getTimeout()
    }

    private fun updateRadius(value: Int) {
        var result = String.format(Locale.getDefault(), radiusOutputText, value)
        if (value == 1) {
            result = String.format(Locale.getDefault(), radiusOutputSingleText, value)
        }
        radiusText = result
    }

    private fun updateTimeout(value: Int) {
        var result = String.format(Locale.getDefault(), timeoutOutputText, value)
        if (value == 1) {
            result = String.format(Locale.getDefault(), timeoutOutputSingleText, value)
        }
        timeoutText = result
    }
}

@BindingAdapter("timeOutProgressListener")
fun setTimeoutProgressListener(view: AppCompatSeekBar, viewModel: SettingsViewModel) {
    view.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (progress < 1) {
                seekBar.progress = 1
            } else {
                viewModel.timeoutProgress = progress
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            viewModel.saveTimeOutProgress()
        }
    })
}


@BindingAdapter("radiusProgressListener")
fun setRadiusProgressListener(view: AppCompatSeekBar, viewModel: SettingsViewModel) {
    view.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (progress < 1) {
                seekBar.progress = 1
            } else {
                viewModel.radiusProgress = progress
            }
        }
        override fun onStartTrackingTouch(seekBar: SeekBar) {}

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            viewModel.saveRadiusProgress()
        }
    })
}