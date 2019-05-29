package org.helpapaw.helpapaw.viewmodels

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel

abstract class BaseViewModel:ViewModel(),Observable{

    private val callbacks:PropertyChangeRegistry = PropertyChangeRegistry()

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.remove(callback)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }


    fun notifyChange(){
        callbacks.notifyChange(this,0)
    }

    fun notifyChange(viewId:Int){
        callbacks.notifyChange(this,viewId)
    }
}