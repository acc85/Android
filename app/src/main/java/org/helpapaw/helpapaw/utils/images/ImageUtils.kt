package org.helpapaw.helpapaw.utils.images

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.*

class ImageUtils(){

    companion object {
        private const val SCHEME_FILE = "file"
        private const val SCHEME_CONTENT = "content"
    }

    fun closeSilently(c: Closeable?) {
        if (c == null) return
        try {
            c.close()
        } catch (t: Throwable) {
            // Do nothing
        }

    }

    //Files
    fun getPhotoFileUri(context: Context?, fileName: String): Uri? {
        val APP_TAG = "HelpAPaw"
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            val mediaStorageDir = File(
                    context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG)

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(APP_TAG, "failed to create directory")
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(File(mediaStorageDir.path + File.separator + fileName))
        }
        return null
    }

    fun getExifRotation(imageFile: File?): Int {
        if (imageFile == null) return 0
        try {
            val exif = ExifInterface(imageFile.absolutePath)
            // We only recognize a subset of orientation tag values
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        } catch (e: IOException) {
            //  Log.e("Error getting Exif data", e);
            return 0
        }

    }

    fun getRotatedBitmap(src: File): Bitmap? {
        val bitmap = decodeFile(src, 500, 500)
        val orientation = getExifRotation(src)

        if (orientation == 1) {
            return bitmap
        }

        val matrix = Matrix()
        when (orientation) {
            2 -> matrix.setScale(-1f, 1f)
            3 -> matrix.setRotate(180f)
            4 -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            5 -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            6 -> matrix.setRotate(90f)
            7 -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            8 -> matrix.setRotate(-90f)
            else -> return bitmap
        }

        try {
            val oriented = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
            bitmap.recycle()
            return oriented
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            return bitmap
        }
    }

    fun decodeFile(f: File, WIDTH: Int, HEIGHT: Int): Bitmap? {
        try {
            //Decode image size
            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(f), null, bitmapOptions)

            var scale = 1
            while (bitmapOptions.outWidth / scale / 2 >= WIDTH && bitmapOptions.outHeight / scale / 2 >= HEIGHT)
                scale *= 2

            val bitmapNewOptions = BitmapFactory.Options()
            bitmapNewOptions.inSampleSize = scale
            return BitmapFactory.decodeStream(FileInputStream(f), null, bitmapNewOptions)
        } catch (e: FileNotFoundException) {
        }

        return null
    }

    fun getFromMediaUri(context: Context?, resolver: ContentResolver, uri: Uri?): File? {
        if (uri == null) return null

        if (SCHEME_FILE == uri.scheme) {
            return File(uri.path!!)
        } else if (SCHEME_CONTENT == uri.scheme) {
            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)
            var cursor: Cursor? = null
            try {
                cursor = resolver.query(uri, filePathColumn, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex: Int
                    if (uri.toString().startsWith("content://com.google.android.gallery3d")) {
                        columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    } else {
                        columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                    }
                    if (columnIndex != -1) {
                        val filePath = cursor.getString(columnIndex)
                        if (!TextUtils.isEmpty(filePath)) {
                            return File(filePath)
                        }
                    }
                }
            } catch (e: IllegalArgumentException) {
                // Google Drive images
                return getFromMediaUriPfd(context, resolver, uri)
            } catch (ignored: SecurityException) {
                // Nothing we can do
            } finally {
                cursor?.close()
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun getTempFilename(context: Context?): String {
        val outputDir = context?.cacheDir
        val outputFile = File.createTempFile("image", "tmp", outputDir)
        return outputFile.absolutePath
    }

    private fun getFromMediaUriPfd(context: Context?, resolver: ContentResolver, uri: Uri?): File? {
        if (uri == null) return null

        lateinit var input: FileInputStream
        lateinit var output: FileOutputStream
        try {
            val pfd = resolver.openFileDescriptor(uri, "r")
            val fd = pfd!!.fileDescriptor
            input = FileInputStream(fd)

            val tempFilename = getTempFilename(context)
            output = FileOutputStream(tempFilename)

            input.copyTo(output, bufferSize = DEFAULT_BUFFER_SIZE)

            return File(tempFilename)
        } catch (ignored: IOException) {
            // Nothing we can do
        } finally {
            closeSilently(input)
            closeSilently(output)
        }
        return null
    }

    // Returns true if external storage for photos is available
    private fun isExternalStorageAvailable(): Boolean {
        val state = Environment.getExternalStorageState()
        return state == Environment.MEDIA_MOUNTED
    }
}