package org.helpapaw.helpapaw.data.repositories

import android.content.Context
import android.graphics.Bitmap
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import org.helpapaw.helpapaw.base.PawApplication
import org.helpapaw.helpapaw.utils.images.ImageUtils
import java.io.File

/**
 * Created by iliyan on 8/1/16
 */
class BackendlessPhotoRepository : PhotoRepository {

    override fun savePhoto(photoUri: String?, photoName: String, callback: PhotoRepository.SavePhotoCallback) {
        val photo = ImageUtils.getInstance().getRotatedBitmap(File(photoUri))
        Backendless.Files.Android.upload(photo,
                Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, photoName + PHOTO_EXTENSION,
                PHOTOS_DIRECTORY, true, object : AsyncCallback<BackendlessFile> {
            override fun handleResponse(backendlessFile: BackendlessFile) {
                callback.onPhotoSaved()
            }

            override fun handleFault(backendlessFault: BackendlessFault) {
                callback.onPhotoFailure(backendlessFault.getMessage())
            }
        })
    }

    override fun getPhotoUrl(signalId: String): String? {
        return if (signalId != null) {
            //https://api.backendless.com/<application id>/<REST-api-key>/files/<path>/<file name>
            BACKENDLESS_API_DOMAIN +
                    PawApplication.BACKENDLESS_APP_ID + "/" +
                    PawApplication.BACKENDLESS_REST_API_KEY + "/" +
                    FILES_FOLDER + "/" +
                    PHOTOS_DIRECTORY + "/" +
                    signalId +
                    PHOTO_EXTENSION
        } else {
            null
        }
    }

    companion object {

        private val BACKENDLESS_API_DOMAIN = "https://api.backendless.com/"
        private val FILES_FOLDER = "files"
        private val PHOTOS_DIRECTORY = "signal_photos"
        private val PHOTO_EXTENSION = ".jpg"

        private val PHOTO_QUALITY = 60
    }


}