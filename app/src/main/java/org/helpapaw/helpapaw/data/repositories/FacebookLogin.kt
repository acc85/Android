package org.helpapaw.helpapaw.data.repositories

import androidx.appcompat.app.AppCompatActivity
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.facebook.CallbackManager

interface FacebookLogin{

    fun loginWithFacebook(activity: AppCompatActivity, callbackManager: CallbackManager, asyncCallback: AsyncCallback<BackendlessUser>)
}