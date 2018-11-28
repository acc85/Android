package org.helpapaw.helpapaw.data.models.backendless.repositories

import android.graphics.Bitmap
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import org.helpapaw.helpapaw.base.PawApplication
import org.helpapaw.helpapaw.utils.images.ImageUtils
import java.io.File
class BackendlessPhotoRepository():PhotoRepository{

    constructor(imageUtils: ImageUtils):this(){
        this.imageUtils = imageUtils
    }

    lateinit var imageUtils: ImageUtils

    companion object {
        private const val BACKENDLESS_API_DOMAIN = "https://api.backendless.com/"
        private const val FILES_FOLDER = "files"
        private const val PHOTOS_DIRECTORY = "signal_photos"
        private const val PHOTO_EXTENSION = ".jpg"

        private const val PHOTO_QUALITY = 60
    }


    override fun getPhotoUrl(signalId: String?): String {
        if (signalId != null) run {
            //https://api.backendless.com/<application id>/<REST-api-key>/files/<path>/<file name>
            return BACKENDLESS_API_DOMAIN +
                    PawApplication.BACKENDLESS_APP_ID + "/" +
                    PawApplication.BACKENDLESS_REST_API_KEY + "/" +
                    FILES_FOLDER + "/" +
                    PHOTOS_DIRECTORY + "/" +
                    signalId +
                    PHOTO_EXTENSION
        }else{
            return ""
        }
    }

    override fun savePhoto(photoUri: String, photoName: String, callback: PhotoRepository.SavePhotoCallback) {
        val photo = imageUtils.getRotatedBitmap(File(photoUri))
        Backendless.Files.Android.upload(photo,
                Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, photoName + PHOTO_EXTENSION,
                PHOTOS_DIRECTORY, true, object : AsyncCallback<BackendlessFile> {

            override fun handleFault(backendlessFault: BackendlessFault?) {
                callback.onPhotoFailure(backendlessFault?.message?: "Empty Fault")
            }


            override fun handleResponse(response: BackendlessFile?) {
                callback.onPhotoSaved()
            }

        })
    }

}