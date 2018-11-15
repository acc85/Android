package org.helpapaw.helpapaw.base

import io.fabric.sdk.android.services.concurrency.AsyncTask.init
import java.util.HashMap

class PresenterManager(val presenterMap: MutableMap<String, Presenter<*>> = HashMap()) {


    fun putPresenter(id: String, presenter: Presenter<*>) {
        presenterMap[id] = presenter
    }

    fun <T : Presenter<*>> getPresenter(id: String): T? {
        return presenterMap[id] as T
    }

    fun remove(id: String) {
        presenterMap.remove(id)
    }

    fun removeAll() {
        presenterMap.clear()
    }

    companion object {
        val instance: PresenterManager = PresenterManager()
    }
}