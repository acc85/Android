package org.helpapaw.helpapaw.base

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

import org.helpapaw.helpapaw.R

import java.util.UUID

/**
 * Created by iliyan on 6/22/16
 */
abstract class BaseFragment : Fragment() {

    var screenId: String? = null
        protected set
    private var onSaveInstanceCalled = false

//    protected abstract val presenter: Presenter<*>?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            screenId = UUID.randomUUID().toString()
        } else {
            screenId = savedInstanceState.getString(SCREEN_ID)
            onSaveInstanceCalled = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SCREEN_ID, screenId)
//        if (presenter != null) {
//            onSaveInstanceCalled = true
////            PresenterManager.getInstance().putPresenter(screenId!!, presenter)
//        }
    }

    override fun onDestroy() {
//        if (presenter == null) {
//            //no viewModel for this fragment
//            super.onDestroy()
//            return
//        }
//        if (activity != null && activity!!.isFinishing) {
//            presenter!!.clearView()
////            PresenterManager.getInstance().remove(screenId)
//        } else if (this.isRemoving && !onSaveInstanceCalled) {
//            // The fragment can be still in back stack even if isRemoving() is true.
//            // We check onSaveInstanceCalled - if this was not called then the fragment is totally removed.
//            presenter!!.clearView()
////            PresenterManager.getInstance().remove(screenId)
//        }
        super.onDestroy()
    }

    protected fun openFragment(fragmentToOpen: Fragment, addToBackStack: Boolean, shouldAnimate: Boolean, animateBothDirections: Boolean) {
        if (activity != null) {
            val toolbar:Toolbar? = activity?.findViewById(R.id.toolbar)
            toolbar?.menu?.clear()
            val ft = activity!!.supportFragmentManager.beginTransaction()
            if (shouldAnimate) {
                if (animateBothDirections) {
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left,
                            R.anim.slide_in_right, R.anim.slide_out_right)
                } else {
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                }
            }
            ft.replace(R.id.grp_content_frame, fragmentToOpen)
            if (addToBackStack) {
                ft.addToBackStack(null)
            }

            ft.commit()
        }
    }

    protected open fun hideKeyboard() {
        val view = activity!!.currentFocus
        if (view != null) {
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {

        val SCREEN_ID = "screenId"
    }

}
