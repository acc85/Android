package org.helpapaw.helpapaw.signalphoto

import org.helpapaw.helpapaw.data.models.Signal

interface SignalPhotoContract{

    interface View {

        fun showSignalPhoto(signal: Signal)
    }

    interface UserActionsListener {

        fun onInitPhotoScreen(signal: Signal)
    }
}