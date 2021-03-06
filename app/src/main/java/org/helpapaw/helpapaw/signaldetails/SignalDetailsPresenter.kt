package org.helpapaw.helpapaw.signaldetails

import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.data.models.Comment
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.data.models.backendless.repositories.*
import org.helpapaw.helpapaw.data.user.BackendlessUserManager
import org.helpapaw.helpapaw.data.user.UserManager
import org.helpapaw.helpapaw.utils.Utils
import javax.inject.Inject


class SignalDetailsPresenter(override var view: SignalDetailsContract.View?) : Presenter<SignalDetailsContract.View>(view), SignalDetailsContract.UserActionsListener {

    @Inject
    constructor(signalDetailsFragment: SignalDetailsFragment):this(signalDetailsFragment as SignalDetailsContract.View)

    private var showProgressBar: Boolean = false
    private var commentList: MutableList<Comment>? = null
    lateinit var signal: Signal

    private var statusChanged: Boolean = false

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var commentRepository: CommentRepository

    @Inject
    lateinit var photoRepository: BackendlessPhotoRepository

    @Inject
    lateinit var signalRepository: BackendlessSignalRepository

    @Inject
    lateinit var userManager: BackendlessUserManager

    init{
        showProgressBar = true
        statusChanged = false
    }


    override fun onInitDetailsScreen(signal: Signal?) {
        setProgressIndicator(showProgressBar)
        if (signal != null) {
            this.signal = signal
            signal.photoUrl = (photoRepository.getPhotoUrl(signal.id))
            view?.showSignalDetails(signal)

            if (commentList != null) {
                setProgressIndicator(false)

                if (commentList?.size == 0) {
                    view?.setNoCommentsTextVisibility(true)
                } else {
                    view?.displayComments(commentList)
                    view?.setNoCommentsTextVisibility(false)
                }
            } else {
                loadCommentsForSignal(signal.id)
            }
        }
    }

    override fun loadCommentsForSignal(signalId: String) {
        if (utils.hasNetworkConnection()) {
            commentRepository.getAllCommentsBySignalId(signalId, object : CommentRepository.LoadCommentsCallback {
                override fun onCommentsLoaded(comments: List<Comment>) {
                    if (!isViewAvailable()) return
                    commentList = comments.toMutableList()
                    setProgressIndicator(false)

                    if (commentList?.size == 0) {
                        view!!.setNoCommentsTextVisibility(true)
                    } else {
                        view!!.displayComments(comments)
                        view!!.setNoCommentsTextVisibility(false)
                    }
                }

                override fun onCommentsFailure(message: String) {
                    if (!isViewAvailable()) return
                    view!!.showMessage(message)
                }
            })
        } else {
            if (isViewAvailable()) {
                view!!.showNoInternetMessage()
            }
        }
    }

    override fun onAddCommentButtonClicked(comment: String?) {
        if (utils.hasNetworkConnection()) {
            if (comment != null && comment.trim().isNotEmpty()) {
                view!!.hideKeyboard()
                setProgressIndicator(true)
                view!!.scrollToBottom()

                userManager.isLoggedIn(object : UserManager.LoginCallback {
                    override fun onLoginSuccess() {
                        if (!isViewAvailable()) return
                        view!!.clearSendCommentView()
                        saveComment(comment, signal.id)
                    }

                    override fun onLoginFailure(message: String) {
                        if (!isViewAvailable()) return
                        setProgressIndicator(false)
                        view!!.openLoginScreen()
                    }
                })

            } else {
                view!!.showCommentErrorMessage()
            }
        } else {
            view!!.showNoInternetMessage()
        }
    }

    override fun onRequestStatusChange(status: Int) {
        if (utils.hasNetworkConnection()) {

            userManager.isLoggedIn(object : UserManager.LoginCallback {
                override fun onLoginSuccess() {
                    signalRepository.updateSignalStatus(signal.id, status, object : SignalRepository.UpdateStatusCallback {
                        override fun onStatusUpdated(status: Int) {
                            if (!isViewAvailable()) return
                            setSignalStatus(status)
                            view!!.showStatusUpdatedMessage()
                            view!!.onStatusChangeRequestFinished(true, status)
                        }

                        override fun onStatusFailure(message: String) {
                            if (!isViewAvailable()) return
                            view!!.showMessage(message)
                            view!!.onStatusChangeRequestFinished(false, 0)
                        }
                    })
                }

                override fun onLoginFailure(message: String) {
                    if (!isViewAvailable()) return
                    view!!.onStatusChangeRequestFinished(false, 0)
                    view!!.openLoginScreen()
                }
            })
        } else {
            view!!.showNoInternetMessage()
        }
    }

    override fun onCallButtonClicked() {
        val phoneNumber = signal.authorPhone
        view?.openNumberDialer(phoneNumber)
    }

    override fun onSignalDetailsClosing() {
        view?.closeScreenWithResult(signal)
    }

    override fun onBottomReached(isBottomReached: Boolean) {
        view?.setShadowVisibility(!isBottomReached)
    }

    override fun onSignalPhotoClicked() {
        view?.openSignalPhotoScreen()
    }

    private fun saveComment(comment: String, signalId: String) {
        commentRepository?.saveComment(comment, signalId, object : CommentRepository.SaveCommentCallback {
            override fun onCommentSaved(comment: Comment) {
                if (!isViewAvailable()) return
                setProgressIndicator(false)
                commentList?.add(comment)
                view!!.setNoCommentsTextVisibility(false)
                view!!.displayComments(commentList)
            }

            override fun onCommentFailure(message: String) {
                if (!isViewAvailable()) return
                view!!.showMessage(message)
            }
        })
    }


    private fun setSignalStatus(status: Int) {
        this.signal.status = status
        statusChanged = true
    }

    private fun isViewAvailable(): Boolean {
        return view != null && view!!.isActive()
    }

    private fun setProgressIndicator(active: Boolean) {
        view!!.setProgressIndicator(active)
        this.showProgressBar = active
    }
}