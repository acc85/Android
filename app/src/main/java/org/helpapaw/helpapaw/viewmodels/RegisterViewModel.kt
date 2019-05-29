package org.helpapaw.helpapaw.viewmodels

import android.view.View
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.runBlocking
import org.helpapaw.helpapaw.BR
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.utils.Utils

sealed class RegisterResult {
    data class ShowWhyDialog(val nothing: String = "") : RegisterResult()
    data class ShowErrorMessage(val message: String): RegisterResult()
    data class ShowPrivacyDialog(val messaage:String) : RegisterResult()
}


class RegisterViewModel(
        val utils:Utils

) : BaseViewModel() {

    var registerLiveData: MutableLiveData<RegisterResult> = MutableLiveData()

    @Bindable
    var progressVisibility: Int = View.GONE
        set(value) {
            field = value
            notifyChange(BR.progressVisibility)
        }

    @Bindable
    var groupVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            notifyChange(BR.groupVisibility)
        }

    @Bindable
    var emailAddress: String = ""

    @Bindable
    var password: String = ""

    @Bindable
    var confirmPassword: String = ""

    @Bindable
    var name: String = ""

    @Bindable
    var phone: String = ""

    @Bindable
    var errorEmail: String? = null
        set(value) {
            field = value
            notifyChange(BR.errorEmail)
        }

    @Bindable
    var errorPassword: String? = null
        set(value) {
            field = value
            notifyChange(BR.errorPassword)
        }


    @Bindable
    var errorConfirmPassword: String? = null
        set(value) {
            field = value
            notifyChange(BR.errorConfirmPassword)
        }

    @Bindable
    var errorName: String? = null
        set(value) {
            field = value
            notifyChange(BR.errorName)
        }

    fun showProgress(show: Boolean){
        progressVisibility = if(show) View.VISIBLE  else View.GONE
        groupVisibility =if(show) View.GONE  else View.VISIBLE
    }

    fun login(view: View) {
        if(verify(view)){
            runBlocking {
                val result: String? = Utils.getHtmlByCouroutines(view.context.getString(R.string.url_privacy_policy)).await()
                showProgress(false)
                if (result != null) {
                    registerLiveData.value = RegisterResult.ShowPrivacyDialog(result)
                } else {
                    registerLiveData.value = RegisterResult.ShowErrorMessage(view.context.getString(R.string.txt_error_getting_privacy_policy))
                }
            }
        }
    }

    fun register(view: View) {

    }

    fun whyPhone(view: View) {
        registerLiveData.value = RegisterResult.ShowWhyDialog()
    }

    fun verify(view:View):Boolean{
        var isValid  = true
        if (emailAddress.isBlank() || !utils.isEmailValid(emailAddress)) {
            errorEmail = view.context.getString(R.string.txt_invalid_email)
            isValid = false
        }

        if (password.isBlank() || password.length < MIN_PASS_LENGTH) {
            errorPassword = view.context.getString(R.string.txt_invalid_password)
            isValid = false
        }

        if (password != confirmPassword) {
            errorConfirmPassword = view.context.getString(R.string.txt_invalid_password_confirmation)
            isValid = false
        }

        if (name.isBlank()) {
            errorName = view.context.getString(R.string.txt_name_required)
            isValid = false
        }
        return isValid
    }
}