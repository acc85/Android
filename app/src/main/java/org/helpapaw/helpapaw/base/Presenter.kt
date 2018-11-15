package org.helpapaw.helpapaw.base

abstract class Presenter<T>(open var view: T?) {

    fun clearView() {
        view = null
    }

}