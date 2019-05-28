package org.helpapaw.helpapaw.signaldetails

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.models.Comment
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.databinding.FragmentSignalDetailsBinding
import org.helpapaw.helpapaw.images.ImageLoader
import org.helpapaw.helpapaw.signalphoto.SignalPhotoActivity

import org.helpapaw.helpapaw.models.Comment.COMMENT_TYPE_STATUS_CHANGE
import org.helpapaw.helpapaw.utils.StatusUtils
import org.helpapaw.helpapaw.utils.Utils
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class SignalDetailsFragment : BaseFragment(), SignalDetailsContract.View {

    val signalDetailsPresenter: SignalDetailsPresenter by inject{
        parametersOf(this)
    }

    val imageLoader:ImageLoader by inject()

    val utils:Utils by inject()
    lateinit var actionsListener: SignalDetailsContract.UserActionsListener

    lateinit var binding: FragmentSignalDetailsBinding

    private var mSignal: Signal? = null

    /* OnClick Listeners */
    val onAddCommentClickListener: View.OnClickListener
        get() = View.OnClickListener {
            val commentText = binding.editComment.text.toString()
            actionsListener.onAddCommentButtonClicked(commentText)
        }

    val onCallButtonClickListener: View.OnClickListener
        get() = View.OnClickListener { actionsListener.onCallButtonClicked() }

    val onBottomReachedListener: InteractiveScrollView.OnBottomReachedListener
        get() = object: InteractiveScrollView.OnBottomReachedListener{
            override fun onBottomReached(isBottomReached: Boolean) {
                actionsListener.onBottomReached(isBottomReached)
            }
        }

    val onSignalPhotoClickListener: View.OnClickListener
        get() = View.OnClickListener { actionsListener.onSignalPhotoClicked() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signal_details, container, false)
        if (savedInstanceState != null) {
            signalDetailsPresenter.view = this
        }

        actionsListener = signalDetailsPresenter
        setHasOptionsMenu(true)
        mSignal = null
        if (arguments != null) {
            mSignal = arguments!!.getParcelable(SIGNAL_DETAILS)
        }
        mSignal?.let{signal->
            actionsListener.onInitDetailsScreen(signal)
        }

        binding.btnAddComment.setOnClickListener(onAddCommentClickListener)
        binding.imgCall.setOnClickListener(onCallButtonClickListener)
        binding.btnCall.setOnClickListener(onCallButtonClickListener)
        binding.imgSignalPhoto.setOnClickListener(onSignalPhotoClickListener)
        binding.scrollSignalDetails.setOnBottomReachedListener(onBottomReachedListener)
        binding.viewSignalStatus.setStatusCallback(getStatusViewCallback())

        return binding.root
    }


    private fun getStatusViewCallback(): StatusCallback {
        return object : StatusCallback {
            override fun onRequestStatusChange(status: Int) {
                actionsListener.onRequestStatusChange(status)
            }
        }
    }

    override fun showMessage(message: String) {
        if (view != null) {
            Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun setProgressIndicator(active: Boolean) {
        binding.progressComments.visibility = if (active) View.VISIBLE else View.GONE
    }

//    override fun getPresenter(): Presenter<*>? {
//        return signalDetailsPresenter
//    }

    override fun hideKeyboard() {
        super.hideKeyboard()
    }

    override fun showSignalDetails(signal: Signal) {
        binding.txtSignalTitle.text = signal.title
        binding.txtSignalAuthor.text = signal.authorName

        val formattedDate = utils.getFormattedDate(signal.dateSubmitted)
        binding.txtSubmittedDate.text = formattedDate
        binding.viewSignalStatus.updateStatus(signal.status)

        val authorPhone = signal.authorPhone
        if (authorPhone == null || authorPhone.trim { it <= ' ' }.isEmpty()) {
            binding.imgCall.visibility = View.GONE
            binding.btnCall.visibility = View.GONE
        } else {
            binding.imgCall.visibility = View.VISIBLE
            binding.btnCall.text = authorPhone
            binding.btnCall.visibility = View.VISIBLE
        }

        imageLoader.loadWithRoundedCorners(context, signal.photoUrl, binding.imgSignalPhoto, R.drawable.ic_paw)
    }

    override fun displayComments(comments: List<Comment>) {
        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        binding.grpComments.removeAllViews()

        for (i in comments.indices) {
            val comment = comments[i]

            val inflatedCommentView: View
            val commentText: String

            if (comment.type == COMMENT_TYPE_STATUS_CHANGE) {
                inflatedCommentView = inflater.inflate(R.layout.view_comment_status_change, binding.grpComments, false)

                // First try to get the new status code
                val newStatus = Comment.getNewStatusFromStatusChangeComment(comment)
                // Then get the string for that status
                val statusString = StatusUtils.getStatusStringForCode(newStatus)
                // Finally form the string to be displayed as comment
                commentText = String.format(getString(R.string.txt_user_changed_status_to), comment.ownerName, statusString)

                // Set the icon for the new status
                val imgNewStatusIcon = inflatedCommentView.findViewById(R.id.img_new_status_icon) as ImageView
                imgNewStatusIcon.setImageResource(StatusUtils.getPinResourceForCode(newStatus))
            } else {
                inflatedCommentView = inflater.inflate(R.layout.view_comment, binding.grpComments, false)

                val txtCommentAuthor = inflatedCommentView.findViewById(R.id.txt_comment_author) as TextView
                txtCommentAuthor.text = comment.ownerName

                commentText = comment.text
            }

            // text and date elements are common for both type of comments so they are set in common code
            val txtCommentText = inflatedCommentView.findViewById(R.id.txt_comment_text) as TextView
            val txtCommentDate = inflatedCommentView.findViewById(R.id.txt_comment_date) as TextView

            txtCommentText.text = commentText

            val formattedDate = utils.getFormattedDate(comment.dateCreated)
            txtCommentDate.text = formattedDate

            binding.grpComments.addView(inflatedCommentView)
        }
    }

    override fun showCommentErrorMessage() {
        binding.editComment.error = getString(R.string.txt_error_empty_comment)
    }

    override fun clearSendCommentView() {
        binding.editComment.error = null
        binding.editComment.text = null
    }

    override fun openLoginScreen() {
        val intent = Intent(context, AuthenticationActivity::class.java)
        startActivity(intent)
    }

    override fun setNoCommentsTextVisibility(visibility: Boolean) {
        if (visibility) {
            binding.txtNoComments.visibility = View.VISIBLE
            setShadowVisibility(false)
        } else {
            binding.txtNoComments.visibility = View.GONE
        }
    }

    override fun showNoInternetMessage() {
        showMessage(getString(R.string.txt_no_internet))
    }

    override fun scrollToBottom() {
        binding.scrollSignalDetails.post { binding.scrollSignalDetails.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    override fun showStatusUpdatedMessage() {
        showMessage(getString(R.string.txt_status_updated))
    }

    override fun openNumberDialer(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_signal_details, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return super.onOptionsItemSelected(item)
    }

    override fun isActive(): Boolean {
        return isAdded
    }

    override fun closeScreenWithResult(signal: Signal) {
        val data = Intent()
        data.putExtra("signal", signal)
        activity?.setResult(Activity.RESULT_OK, data)

        activity?.finish()
    }

    override fun setShadowVisibility(visibility: Boolean) {
        if (visibility) {
            binding.viewShadow.animate().alpha(1f)
        } else {
            binding.viewShadow.animate().alpha(0f)
        }
    }

    override fun openSignalPhotoScreen() {
        val intent = Intent(context, SignalPhotoActivity::class.java)
        intent.putExtra(SignalDetailsActivity.SIGNAL_KEY, mSignal)
        startActivity(intent)
    }

    fun onBackPressed() {
        actionsListener.onSignalDetailsClosing()
    }

    override fun onStatusChangeRequestFinished(success: Boolean, newStatus: Int) {

        if (success) {
            actionsListener.loadCommentsForSignal(mSignal!!.id)
        }

        binding.viewSignalStatus.onStatusChangeRequestFinished(success, newStatus)
    }

    companion object {

        private val SIGNAL_DETAILS = "signalDetails"
        private val TAG = SignalDetailsFragment::class.java.simpleName

        fun newInstance(signal: Signal): SignalDetailsFragment {
            val fragment = SignalDetailsFragment()
            val bundle = Bundle()
            bundle.putParcelable(SIGNAL_DETAILS, signal)
            fragment.arguments = bundle
            return fragment
        }
    }
}// Required empty public constructor
