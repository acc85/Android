package org.helpapaw.helpapaw.settings

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.databinding.FragmentSettingsBinding
import org.helpapaw.helpapaw.viewmodels.SettingsViewModel
import org.koin.android.ext.android.inject
class SettingsFragment : BaseFragment(){

    private val settingsViewModel: SettingsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding:FragmentSettingsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        (activity as? AppCompatActivity)?.let {act->
            act.setSupportActionBar(binding.toolbar)
            (activity as? AppCompatActivity)?.supportActionBar?.let{ab->
                ab.setDisplayHomeAsUpEnabled(true)
                ab.setDisplayShowTitleEnabled(false)
            }
        }
        binding.viewModel = settingsViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel.setRadius()
        settingsViewModel.setTimeout()
    }
}