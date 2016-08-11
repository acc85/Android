package org.helpapaw.helpapaw.signaldetails;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.CommentRepository;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by iliyan on 7/25/16
 */
public class SignalDetailsPresenter extends Presenter<SignalDetailsContract.View> implements SignalDetailsContract.UserActionsListener {

    private boolean showProgressBar;
    private List<Comment> commentList;
    private Signal signal;

    private CommentRepository commentRepository;
    private PhotoRepository photoRepository;
    private SignalRepository signalRepository;
    private UserManager userManager;

    public SignalDetailsPresenter(SignalDetailsContract.View view) {
        super(view);
        showProgressBar = true;
        commentRepository = Injection.getCommentRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
        signalRepository = Injection.getSignalRepositoryInstance();
    }

    @Override
    public void onInitDetailsScreen(Signal signal) {
        setProgressIndicator(showProgressBar);
        if (signal != null) {
            this.signal = signal;
            signal.setPhotoUrl(photoRepository.getPhotoUrl(signal.getId()));
            getView().showSignalDetails(signal);

            if (commentList != null) {
                setProgressIndicator(false);

                if (commentList.size() == 0) {
                    getView().setNoCommentsTextVisibility(true);
                } else {
                    getView().displayComments(commentList);
                    getView().setNoCommentsTextVisibility(false);
                }
            } else {
                loadCommentsForSignal(signal.getId());
            }
        }
    }

    private void loadCommentsForSignal(String signalId) {
        commentRepository.getAllCommentsBySignalId(signalId, new CommentRepository.LoadCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<Comment> comments) {
                if (!isViewAvailable()) return;
                commentList = comments;
                setProgressIndicator(false);

                if (commentList.size() == 0) {
                    getView().setNoCommentsTextVisibility(true);
                } else {
                    getView().displayComments(comments);
                    getView().setNoCommentsTextVisibility(false);
                }
            }

            @Override
            public void onCommentsFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
            }
        });
    }

    @Override
    public void onAddCommentButtonClicked(final String comment) {
        if (comment != null && comment.trim().length() > 0) {
            getView().hideKeyboard();
            setProgressIndicator(true);
            getView().scrollToBottom();

            userManager.isLoggedIn(new UserManager.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    if (!isViewAvailable()) return;
                    getView().clearSendCommentView();
                    saveComment(comment, signal.getId());
                }

                @Override
                public void onLoginFailure(String message) {
                    if (!isViewAvailable()) return;
                    setProgressIndicator(false);
                    getView().openLoginScreen();
                }
            });

        } else {
            getView().showCommentErrorMessage();
        }
    }

    @Override
    public void onStatusChanged(int status) {
        if(Utils.getInstance().hasNetworkConnection()) {
            signalRepository.updateSignalStatus(signal.getId(), status, new SignalRepository.UpdateStatusCallback() {
                @Override
                public void onStatusUpdated() {

                }

                @Override
                public void onStatusFailure(String message) {
                    if(!isViewAvailable()) return;
                    getView().showMessage(message);
                }
            });
        } else {
            getView().showNoInternetMessage();
        }
    }

    @Override
    public void onCallButtonClicked() {
        String phoneNumber = signal.getAuthorPhone();
        getView().openNumberDialer(phoneNumber);
    }

    private void saveComment(String comment, String signalId) {
        commentRepository.saveComment(comment, signalId, new CommentRepository.SaveCommentCallback() {
            @Override
            public void onCommentSaved(Comment comment) {
                if (!isViewAvailable()) return;
                setProgressIndicator(false);
                commentList.add(comment);
                getView().setNoCommentsTextVisibility(false);
                getView().displayComments(Collections.singletonList(comment));
            }

            @Override
            public void onCommentFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
            }
        });
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }

    private void setProgressIndicator(boolean active) {
        getView().setProgressIndicator(active);
        this.showProgressBar = active;
    }
}