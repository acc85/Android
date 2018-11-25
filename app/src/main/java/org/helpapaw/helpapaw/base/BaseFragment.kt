package org.helpapaw.helpapaw.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import org.helpapaw.helpapaw.R
import java.util.*

abstract class BaseFragment:DaggerFragment(){

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }


    companion object {
        const val SCREEN_ID:String = "screenId"
    }

    protected lateinit var screenId:String
    private var onSaveInstanceCalled:Boolean  = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            screenId = UUID.randomUUID().toString()
        } else {
            screenId = savedInstanceState.getString(SCREEN_ID)?:""
            onSaveInstanceCalled = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(SCREEN_ID, screenId)
        if (true) {
            onSaveInstanceCalled = true
            PresenterManager.instance.putPresenter(screenId, getPresenter()!!)
        }
    }

    override fun onDestroy() {
        if (false) {
            //no viewModel for this fragment
            super.onDestroy()
            return
        }
        if (activity?.isFinishing == true) {
            getPresenter()?.clearView()
            PresenterManager.instance.remove(screenId)
        } else if (this.isRemoving && !onSaveInstanceCalled) {
            // The fragment can be still in back stack even if isRemoving() is true.
            // We check onSaveInstanceCalled - if this was not called then the fragment is totally removed.
            getPresenter()?.clearView()
            PresenterManager.instance.remove(screenId)
        }
        super.onDestroy()
    }

    protected fun openFragment(fragmentToOpen: Fragment, addToBackStack:Boolean, shouldAnimate:Boolean, animateBothDirections:Boolean) {
        if (activity != null) {
            val toolbar: Toolbar? = activity?.findViewById<Toolbar>(R.id.toolbar)
            toolbar?.menu?.clear()
            val ft: FragmentTransaction? = activity?.supportFragmentManager?.beginTransaction()
            if (shouldAnimate) {
                if (animateBothDirections) {
                    ft?.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left,
                            R.anim.slide_in_right, R.anim.slide_out_right)
                } else {
                    ft?.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                }
            }
            ft?.replace(R.id.grp_content_frame, fragmentToOpen)
            if (addToBackStack) {
                ft?.addToBackStack(null)
            }

            ft?.commit()
        }
    }

    open protected fun hideKeyboard() {
        val view: View? = activity?.currentFocus
        if (view != null) {
            val imm: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }
    }


    protected abstract fun getPresenter():Presenter<*>?
}