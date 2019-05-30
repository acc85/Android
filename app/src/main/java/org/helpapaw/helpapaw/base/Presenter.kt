package org.helpapaw.helpapaw.base

/**
 * Created by iliyan on 6/22/16
 */
abstract class Presenter<T>(var view: T?) {

    fun clearView() {
        this.view = null
    }

}
