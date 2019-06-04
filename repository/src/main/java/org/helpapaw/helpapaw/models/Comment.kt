package org.helpapaw.helpapaw.models

import android.util.Log
import org.json.JSONObject
import java.util.*



/**
 * Created by iliyan on 8/4/16
 */

const val COMMENT_TYPE_USER_COMMENT = "user_comment"
const val COMMENT_TYPE_STATUS_CHANGE = "status_change"

data class Comment(
        val objectId: String? = "",
        val ownerName: String? = "",
        val dateCreated: Date? = Date(),
        val text: String? = "",
        val type: String? = "") {


//    fun Comment(objectId: String, ownerName: String, dateCreated: Date, text: String, type: String): ??? {
//        this.objectId = objectId
//        this.ownerName = ownerName
//        this.dateCreated = dateCreated
//        this.text = text
//        this.type = type
//
//        if (this.dateCreated == null) {
//            this.dateCreated = Date(0)
//        }
//
//        if (this.type == null) {
//            this.type = ""
//        }
//    }
//
    companion object {
        private val TAG = Comment::class.java.simpleName



        fun getNewStatusFromStatusChangeComment(comment: Comment): Int {
            var newStatus = 0
            try {
                val json = JSONObject(comment.text)
                newStatus = json.getInt("new")
            } catch (ex: Exception) {
                Log.d(TAG, String.format("Failed to parse new status from comment. Not a status change comment? %s", ex.message))
            }

            return newStatus
        }
    }
}
