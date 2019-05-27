package org.helpapaw.helpapaw.base

import java.util.HashMap

/**
 * Created by iliyan on 6/22/16
 */
class PresenterManager private constructor() {

    private val presenterMap: MutableMap<String, Presenter<*>>

    init {
        this.presenterMap = HashMap()
    }

    fun <T> putPresenter(id: String, presenter: Presenter<T>) {
        presenterMap[id] = presenter
    }

    fun <T : Presenter<T>> getPresenter(id: String): T {
        return presenterMap[id] as T
    }

    fun remove(id: String?) {
        presenterMap.remove(id)
    }

    fun removeAll() {
        presenterMap.clear()
    }

    companion object {
        private var instance: PresenterManager? = null

        @Synchronized
        fun getInstance(): PresenterManager {
            if (instance == null) {
                instance = PresenterManager()
            }
            return instance!!
        }
    }
}
