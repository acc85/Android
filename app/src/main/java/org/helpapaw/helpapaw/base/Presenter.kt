package org.helpapaw.helpapaw.base

abstract class Presenter<T>(open var view: T?):IPresenter {

    fun clearView() {
        view = null
    }

}