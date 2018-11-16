package org.helpapaw.helpapaw.data.models

import android.util.Log
import org.json.JSONObject
import java.util.*

data class Comment(var objectId: String?, var ownerName: String, var dateCreated: Date = Date(0), var text: String?, var type: String? = ""){

    constructor():this(null,"",Date(),"","")

    companion object {
        private val TAG:String = Comment::class.java.simpleName

        const val COMMENT_TYPE_USER_COMMENT = "user_comment"
        const val COMMENT_TYPE_STATUS_CHANGE = "status_change"

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

