package org.helpapaw.helpapaw.settings

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.databinding.FragmentSettingsBinding
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

import java.util.Locale

class SettingsFragment : BaseFragment(), SettingsContract.View {

    lateinit var binding: FragmentSettingsBinding
    val settingsPresenter: SettingsPresenter by inject{ parametersOf(this)}
    lateinit var actionsListener: SettingsContract.UserActionsListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

    }

    override fun getPresenter(): Presenter<*> {
        return settingsPresenter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        settingsPresenter.view = this
        actionsListener = settingsPresenter

        actionsListener.initialize()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appCompatActivity = activity as AppCompatActivity?
        if (appCompatActivity != null) {
            appCompatActivity.setSupportActionBar(binding.toolbar)
            val supportActionBar = (activity as AppCompatActivity).supportActionBar

            if (supportActionBar != null) {
                supportActionBar.setDisplayHomeAsUpEnabled(true)
                supportActionBar.setDisplayShowTitleEnabled(false)
                binding.toolbarTitle.text = getString(R.string.text_settings)
            }
        }

        binding.radiusValue.setOnSeekBarChangeListener(onRadiusSeekBarChangeListener())
        binding.timeoutValue.setOnSeekBarChangeListener(onTimeoutSeekBarChangeListener())
    }

    override fun onDestroyView() {
        actionsListener.onCloseSettingsScreen()

        super.onDestroyView()
    }

    fun onRadiusSeekBarChangeListener(): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progress < RADIUS_VALUE_MIN) {
                    seekBar.progress = RADIUS_VALUE_MIN
                } else {
                    updateRadius(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                actionsListener.onRadiusChange(seekBar.progress)
            }
        }
    }

    fun onTimeoutSeekBarChangeListener(): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progress < TIMEOUT_VALUE_MIN) {
                    seekBar.progress = TIMEOUT_VALUE_MIN
                } else {
                    updateTimeout(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                actionsListener.onTimeoutChange(seekBar.progress)
            }
        }
    }

    override fun setRadius(radius: Int) {
        binding.radiusValue.progress = radius
        updateRadius(radius)
    }

    override fun setTimeout(timeout: Int) {
        binding.timeoutValue.progress = timeout
        updateTimeout(timeout)
    }

    private fun updateRadius(value: Int) {
        if (value == 1) {
            val result = String.format(Locale.getDefault(), getString(R.string.radius_output_single), value)
            binding.radiusOutput.text = result
        } else {
            val result = String.format(Locale.getDefault(), getString(R.string.radius_output), value)
            binding.radiusOutput.text = result
        }
    }

    private fun updateTimeout(value: Int) {
        if (value == 1) {
            val result = String.format(Locale.getDefault(), getString(R.string.timeout_output_single), value)
            binding.timeoutOutput.text = result
        } else {
            val result = String.format(Locale.getDefault(), getString(R.string.timeout_output), value)
            binding.timeoutOutput.text = result
        }
    }

    companion object {

        private val RADIUS_VALUE_MIN = 1
        private val TIMEOUT_VALUE_MIN = 1

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}