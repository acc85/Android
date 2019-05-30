package org.helpapaw.helpapaw.signalphoto

import org.helpapaw.helpapaw.models.Signal

/**
 * Created by milen on 05/03/18.
 *
 */

interface SignalPhotoContract {
    interface View {

        fun showSignalPhoto(signal: Signal)
    }

    interface UserActionsListener {

        fun onInitPhotoScreen(signal: Signal)
    }
}