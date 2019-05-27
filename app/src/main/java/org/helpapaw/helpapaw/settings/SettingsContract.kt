package org.helpapaw.helpapaw.settings

interface SettingsContract {
    interface View {
        fun setRadius(radius: Int)

        fun setTimeout(timeout: Int)
    }

    interface UserActionsListener {
        fun initialize()

        fun onRadiusChange(radius: Int)

        fun onTimeoutChange(timeout: Int)

        fun onCloseSettingsScreen()
    }
}
