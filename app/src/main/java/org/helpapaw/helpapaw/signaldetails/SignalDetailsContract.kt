package org.helpapaw.helpapaw.signaldetails

import org.helpapaw.helpapaw.models.Comment
import org.helpapaw.helpapaw.models.Signal

/**
 * Created by iliyan on 7/25/16
 */
interface SignalDetailsContract {

    interface View {

        fun isActive(): Boolean

        fun showMessage(message: String)

        fun setProgressIndicator(active: Boolean)

        fun hideKeyboard()

        fun showSignalDetails(signal: Signal)

        fun displayComments(comments: List<Comment>)

        fun showCommentErrorMessage()

        fun clearSendCommentView()

        fun openLoginScreen()

        fun setNoCommentsTextVisibility(visibility: Boolean)

        fun openNumberDialer(phoneNumber: String)

        fun showNoInternetMessage()

        fun scrollToBottom()

        fun showStatusUpdatedMessage()

        fun closeScreenWithResult(signal: Signal)

        fun setShadowVisibility(visibility: Boolean)

        fun onStatusChangeRequestFinished(success: Boolean, newStatus: Int)

        fun openSignalPhotoScreen()
    }

    interface UserActionsListener {

        fun onInitDetailsScreen(signal: Signal?)

        fun loadCommentsForSignal(signalId: String)

        fun onAddCommentButtonClicked(comment: String?)

        fun onRequestStatusChange(status: Int)

        fun onCallButtonClicked()

        fun onSignalDetailsClosing()

        fun onBottomReached(isBottomReached: Boolean)

        fun onSignalPhotoClicked()
    }
}
