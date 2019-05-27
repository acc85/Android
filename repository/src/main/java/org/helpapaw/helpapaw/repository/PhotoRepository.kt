package org.helpapaw.helpapaw.repository

/**
 * Created by iliyan on 8/1/16
 */
interface PhotoRepository {

    fun savePhoto(photoUri: String?, photoName: String, callback: SavePhotoCallback)

    fun getPhotoUrl(signalId: String): String?


    interface SavePhotoCallback {

        fun onPhotoSaved()

        fun onPhotoFailure(message: String)
    }
}
