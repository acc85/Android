package org.helpapaw.helpapaw.utils

import org.helpapaw.helpapaw.data.models.backendless.repositories.*
import org.helpapaw.helpapaw.data.user.BackendlessUserManager
import org.helpapaw.helpapaw.data.user.UserManager
import org.helpapaw.helpapaw.utils.images.ImageLoader
import org.helpapaw.helpapaw.utils.images.PicassoImageLoader

class Injection{

    companion object {
        private var signalRepositoryInstance: SignalRepository? = null


        @Synchronized
        fun getSignalRepositoryInstance(): SignalRepository {
            if (signalRepositoryInstance == null) {
                signalRepositoryInstance = BackendlessSignalRepository()
            }
            return signalRepositoryInstance!!
        }
    }
}