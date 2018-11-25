package org.helpapaw.helpapaw.base

interface IPresenter {
}

abstract class Presenter<T>(open var view: T?):IPresenter {

    fun clearView() {
        view = null
    }

}