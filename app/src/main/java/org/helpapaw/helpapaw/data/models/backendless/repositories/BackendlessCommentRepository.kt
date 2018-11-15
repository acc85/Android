package org.helpapaw.helpapaw.data.models.backendless.repositories

import android.util.Log
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import org.helpapaw.helpapaw.data.models.Comment
import org.helpapaw.helpapaw.data.models.Comment.Companion.COMMENT_TYPE_USER_COMMENT
import org.helpapaw.helpapaw.data.models.backendless.FINComment
import java.text.SimpleDateFormat
import java.util.*

class BackendlessCommentRepository : CommentRepository {

    companion object {
        private const val DATE_TIME_FORMAT = "MM/dd/yyyy hh:mm:ss"
        private const val NAME_FIELD = "name"
        private const val CREATED_FIELD = "created"
    }

    override fun getAllCommentsBySignalId(signalId: String, callback: CommentRepository.LoadCommentsCallback) {
        val comments = ArrayList<Comment>()

        val whereClause = "signalID = '$signalId'"
        val queryBuilder = DataQueryBuilder.create()
        queryBuilder.whereClause = whereClause
        queryBuilder.setPageSize(30)
        queryBuilder.sortBy = listOf(CREATED_FIELD)

        Backendless.Persistence.of(FINComment::class.java).find(queryBuilder, object : AsyncCallback<List<FINComment>> {

            override fun handleFault(fault: BackendlessFault?) {
                callback.onCommentsFailure(fault?.message ?: "Empty fault")
            }

            override fun handleResponse(foundComments: List<FINComment>?) {
                for (currentComment in foundComments ?: ArrayList()) {
                    lateinit var authorName: String
                    lateinit var dateCreated: Date

                    if (currentComment.author != null) {
                        authorName = currentComment.author?.getProperty(NAME_FIELD) as String
                    }

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
        })
    }

    override fun saveComment(commentText: String, signalId: String, callback: CommentRepository.SaveCommentCallback) {
        val dateFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val backendlessComment = FINComment(commentText, currentDate, signalId, COMMENT_TYPE_USER_COMMENT, Backendless.UserService.CurrentUser())

        val commentsStore = Backendless.Data.of(FINComment::class.java)

        commentsStore.save(backendlessComment, object : AsyncCallback<FINComment> {

            override fun handleFault(fault: BackendlessFault?) {
                callback.onCommentFailure(fault?.message?:"Empty Fault")
            }

            override fun handleResponse(newComment: FINComment?) {
                val userList = ArrayList<BackendlessUser>()
                userList.add(Backendless.UserService.CurrentUser())
                commentsStore.setRelation(newComment, "author", userList, object : AsyncCallback<Int> {
                    override fun handleResponse(response: Int?) {
                        newComment?.author = Backendless.UserService.CurrentUser()
                        lateinit var authorName: String
                        lateinit var dateCreated: Date
                        if (newComment?.author != null) {
                            authorName = newComment.author?.getProperty(NAME_FIELD) as String
                        }

                        try {
                            val dateCreatedString = newComment?.created
                            dateCreated = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()).parse(dateCreatedString)
                        } catch (ex: Exception) {
                            Log.d(BackendlessCommentRepository::class.java.name, "Failed to parse comment date.")
                        }
                        val comment = Comment(newComment?.objectId
                                ?: "", authorName, dateCreated, newComment?.text
                                ?: "", COMMENT_TYPE_USER_COMMENT)
                        callback.onCommentSaved(comment)
                    }
                    override fun handleFault(fault: BackendlessFault?) {
                        callback.onCommentFailure(fault?.message ?: "Empty fault")
                    }
                })
            }
        })

    }
}