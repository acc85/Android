package org.helpapaw.helpapaw

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText

@BindingAdapter(value = ["error"])
fun setErrorMessage(view: TextInputEditText, errorText: String?) {
    view.error = errorText
}