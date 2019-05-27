package org.helpapaw.helpapaw.repository

import android.util.Log
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import org.helpapaw.helpapaw.models.Comment
import org.helpapaw.helpapaw.models.Comment.COMMENT_TYPE_USER_COMMENT
import org.helpapaw.helpapaw.models.backendless.FINComment
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by iliyan on 8/4/16
 */
class BackendlessCommentRepository : CommentRepository {

    override fun getAllCommentsBySignalId(signalId: String, callback: CommentRepository.LoadCommentsCallback) {
        val comments = ArrayList<Comment>()

        val whereClause = "signalID = '$signalId'"
        val queryBuilder = DataQueryBuilder.create()
        queryBuilder.whereClause = whereClause
        queryBuilder.setPageSize(30)
        queryBuilder.sortBy = listOf(CREATED_FIELD)

        Backendless.Persistence.of(FINComment::class.java).find(queryBuilder, object : AsyncCallback<List<FINComment>> {
            override fun handleResponse(foundComments: List<FINComment>) {
                for (i in foundComments.indices) {
                    val currentComment = foundComments[i]
                    var authorName: String? = null
                    if (currentComment.author != null) {
                        authorName = getToStringOrNull(currentComment.author.getProperty(NAME_FIELD))
                    }

                    var dateCreated: Date? = null
                    try {
                        val dateCreatedString = currentComment.created
                        val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
                        dateCreated = dateFormat.parse(dateCreatedString)
                    } catch (ex: Exception) {
                        Log.d(BackendlessCommentRepository::class.java.name, "Failed to parse comment date.")
                    }

                    val comment = Comment(currentComment.objectId, authorName, dateCreated, currentComment.text, currentComment.type)
                    comments.add(comment)
                }

                callback.onCommentsLoaded(comments)
            }

            override fun handleFault(fault: BackendlessFault) {
                callback.onCommentsFailure(fault.message)
            }
        })
    }

    override fun saveComment(commentText: String, signalId: String, callback: CommentRepository.SaveCommentCallback) {
        val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val backendlessComment = FINComment(commentText, currentDate, signalId, COMMENT_TYPE_USER_COMMENT, Backendless.UserService.CurrentUser())

        val commentsStore = Backendless.Data.of(FINComment::class.java)
        commentsStore.save(backendlessComment, object : AsyncCallback<FINComment> {
            override fun handleResponse(newComment: FINComment) {

                val userList = ArrayList<BackendlessUser>()
                userList.add(Backendless.UserService.CurrentUser())
                commentsStore.setRelation(newComment, "author", userList,
                        object : AsyncCallback<Int> {
                            override fun handleResponse(response: Int?) {
                                newComment.author = Backendless.UserService.CurrentUser()
                                var authorName: String? = null
                                if (newComment.author != null) {
                                    authorName = getToStringOrNull(newComment.author.getProperty(NAME_FIELD))
                                }

                                var dateCreated: Date? = null
                                try {
                                    val dateCreatedString = newComment.created
                                    val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
                                    dateCreated = dateFormat.parse(dateCreatedString)
                                } catch (ex: Exception) {
                                    Log.d(BackendlessCommentRepository::class.java.name, "Failed to parse comment date.")
                                }

                                val comment = Comment(newComment.objectId, authorName, dateCreated, newComment.text, COMMENT_TYPE_USER_COMMENT)
                                callback.onCommentSaved(comment)
                            }

                            override fun handleFault(fault: BackendlessFault) {
                                callback.onCommentFailure(fault.message)
                            }
                        })
            }

            override fun handleFault(fault: BackendlessFault) {
                callback.onCommentFailure(fault.message)
            }
        })
    }

    private fun getToStringOrNull(`object`: Any?): String? {
        return `object`?.toString()
    }

    companion object {
        private val DATE_TIME_FORMAT = "MM/dd/yyyy hh:mm:ss"
        private val NAME_FIELD = "name"
        private val CREATED_FIELD = "created"
    }
}