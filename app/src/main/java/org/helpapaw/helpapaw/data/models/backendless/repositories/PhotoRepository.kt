package org.helpapaw.helpapaw.data.models.backendless.repositories

interface PhotoRepository {

    fun savePhoto(photoUri: String, photoName: String, callback: SavePhotoCallback)

    fun getPhotoUrl(signalId: String?): String


    interface SavePhotoCallback {

        fun onPhotoSaved()

        fun onPhotoFailure(message: String)
    }
}