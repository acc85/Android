package org.helpapaw.helpapaw.settings

import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.repository.ISettingsRepository

class SettingsPresenter internal constructor(view: SettingsContract.View, val settingsRepository: ISettingsRepository) : Presenter<SettingsContract.View>(view), SettingsContract.UserActionsListener {

    private var radius: Int = 0
    private var timeout: Int = 0

    override fun initialize() {
        radius = settingsRepository.getRadius()
        timeout = settingsRepository.getTimeout()

        view?.setRadius(radius)
        view?.setTimeout(timeout)

        settingsRepository.clearLocationData()
    }

    override fun onRadiusChange(radius: Int) {
        this.radius = radius
        settingsRepository.saveRadius(radius)
    }

    override fun onTimeoutChange(timeout: Int) {
        this.timeout = timeout
        settingsRepository.saveTimeout(timeout)
    }

    override fun onCloseSettingsScreen() {}
}
