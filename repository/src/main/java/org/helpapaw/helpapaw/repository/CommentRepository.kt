package org.helpapaw.helpapaw.repository

import org.helpapaw.helpapaw.models.Comment


/**
 * Created by iliyan on 8/4/16
 */
interface CommentRepository {

    fun getAllCommentsBySignalId(signalId: String, callback: LoadCommentsCallback)

    fun saveComment(commentText: String, signalId: String, callback: SaveCommentCallback)


    interface LoadCommentsCallback {

        fun onCommentsLoaded(comments: MutableList<Comment>)

        fun onCommentsFailure(message: String)
    }

    interface SaveCommentCallback {

        fun onCommentSaved(comment: Comment)

        fun onCommentFailure(message: String)
    }
}