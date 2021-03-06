package org.helpapaw.helpapaw.data.models.backendless.repositories

import androidx.appcompat.app.AppCompatActivity
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.facebook.CallbackManager
import javax.inject.Inject

class BackendlessFacebookLogin @Inject constructor(): FacebookLogin{

    override fun loginWithFacebook(activity: AppCompatActivity, callbackManager: CallbackManager, asyncCallback:AsyncCallback<BackendlessUser>){
        Backendless.UserService.loginWithFacebookSdk(activity,callbackManager, asyncCallback,true)
    }
}