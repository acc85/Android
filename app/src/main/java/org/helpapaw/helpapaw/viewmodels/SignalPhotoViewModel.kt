package org.helpapaw.helpapaw.viewmodels

import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import com.github.chrisbanes.photoview.PhotoView
import org.helpapaw.helpapaw.BR
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.images.ImageLoader
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.repository.PhotoRepository

class SignalPhotoViewModel(
        private val photoRepository: PhotoRepository,
        val imageLoader: ImageLoader
) : BaseViewModel() {

    @Bindable
    var imageUri: String = ""
        set(value) {
            field = value
            notifyChange(BR.imageUri)
        }


    fun setPhotoUri(signal:Signal?){
        imageUri = photoRepository.getPhotoUrl(signal?.id?:"")?:""
    }
}

@BindingAdapter("setImageWithUri")
fun setImageWithUrl(view: PhotoView, viewModel: SignalPhotoViewModel) {
    viewModel.imageLoader.load(view.context, viewModel.imageUri, view, R.drawable.no_image)
}