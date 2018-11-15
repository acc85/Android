package org.helpapaw.helpapaw.data.models.backendless.repositories

import org.helpapaw.helpapaw.data.models.Comment

interface CommentRepository {

    fun getAllCommentsBySignalId(signalId: String, callback: LoadCommentsCallback)

    fun saveComment(commentText: String, signalId: String, callback: SaveCommentCallback)


    interface LoadCommentsCallback {

        fun onCommentsLoaded(comments: List<Comment>)

        fun onCommentsFailure(message: String)
    }

    interface SaveCommentCallback {

        fun onCommentSaved(comment: Comment)

        fun onCommentFailure(message: String)
    }
}